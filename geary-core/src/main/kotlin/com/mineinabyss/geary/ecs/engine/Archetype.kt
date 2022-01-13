package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.contains
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongArrayList
import java.util.*

/**
 * Archetypes store a list of entities with the same [GearyType], and provide functions to
 * quickly move them between archetypes.
 *
 * An example use case: If a query matches an archetype, it will also match all entities inside which
 * gives a large performance boost to system iteration.
 */
public data class Archetype(
    public val type: GearyType,
    public val id: Int
) {
    /** The entity ids in this archetype. Indices are the same as [componentData]'s sub-lists. */
    //TODO aim to make private
    internal val ids: LongArrayList = LongArrayList()

    /** Component ids in the type that are to hold data */
    // Currently all relations must hold data and the HOLDS_DATA bit on them corresponds to the component part.
    private val dataHoldingType = type.filter { it.holdsData() || it.isRelation() }

    /** An outer list with indices for component ids, and sub-lists with data indexed by entity [ids]. */
    internal val componentData: List<MutableList<GearyComponent>> =
        dataHoldingType.inner.map { mutableListOf() }

    /** Edges to other archetypes where a single component has been added. */
    internal val componentAddEdges = Long2ObjectOpenHashMap<Archetype>()

    /** Edges to other archetypes where a single component has been removed. */
    internal val componentRemoveEdges = Long2ObjectOpenHashMap<Archetype>()

    internal val relations = type.inner.mapNotNull { it.toULong().toRelation() }

    /** Map of relation [Relation.value] id to a list of relations with that [Relation.value]. */
    internal val relationsByValue: Long2ObjectOpenHashMap<List<Relation>> = relations
        .groupBy { it.value.id.toLong() }
        .let { Long2ObjectOpenHashMap(it) }

    /** Map of relation [Relation.key] id to a list of relations with that [Relation.key]. */
    internal val relationsByKey: Long2ObjectOpenHashMap<List<Relation>> = relations
        .groupBy { it.key.toLong() }
        .let { Long2ObjectOpenHashMap(it) }

    /** A map of a relation's data type id to full relations that store data of that type. */
    internal val dataHoldingRelations: Long2ObjectOpenHashMap<List<Relation>> by lazy {
        val map = Long2ObjectOpenHashMap<List<Relation>>()
        relationsByValue.forEach { (key, values) ->
            val dataHolding = values.filter { it.key.holdsData() }
            if (dataHolding.isNotEmpty()) map[key] = dataHolding
        }
        map
    }


    /** A map of component ids to index used internally in this archetype (ex. in [componentData])*/
    private val comp2indices = Long2IntOpenHashMap().apply {
        dataHoldingType.forEachIndexed { i, compId -> put(compId.toLong(), i) }
        defaultReturnValue(-1)
    }

    /** The amount of entities stored in this archetype. */
    public val size: Int get() = ids.size

    private val _sourceListeners = mutableSetOf<GearyListener>()
    public val sourceListeners: Set<GearyListener> = _sourceListeners

    private val _targetListeners = mutableSetOf<GearyListener>()
    public val targetListeners: Set<GearyListener> = _targetListeners

    private val _eventHandlers = mutableSetOf<GearyHandler>()

    //TODO update doc
    /** A map of event class type to a set of event handlers which fire on that event. */
    public val eventHandlers: Set<GearyHandler> = _eventHandlers

    // Basically just want a weak set where stuff gets auto removed when it is no longer running
    // We put our iterator and null and this WeakHashMap handles the rest for us.
    private val runningIterators = Collections.newSetFromMap(WeakHashMap<ArchetypeIterator, Boolean>())

    // ==== Helper functions ====

    /** @return This Archetype's [relationsByValue] that are also a part of [matchRelations]. */
    public fun matchedRelationsFor(matchRelations: Collection<RelationValueId>): Map<RelationValueId, List<Relation>> =
        matchRelations
            .filter { it.id.toLong() in relationsByValue }
            .associateWith { relationsByValue[it.id.toLong()]!! } //TODO handle null error

    /**
     * Used to pack data closer together and avoid having hashmaps throughout the archetype.
     *
     * @return The internally used index for this component [id].
     */
    internal fun indexOf(id: GearyComponentId): Int = comp2indices.get(id.toLong())

    /**
     * @return The data under a [componentId] for an entity at [row].
     *
     * @see Record
     */
    public operator fun get(row: Int, componentId: GearyComponentId): GearyComponent? {
        val compIndex = indexOf(componentId)
        if (compIndex == -1) return null

        return componentData[compIndex][row]
    }

    /** @return The entity stored at a given [row] in this archetype. */
    internal fun getEntity(row: Int): GearyEntity = ids.getLong(row).toGeary()

    /** @return Whether this archetype has a [componentId] in its type, regardless of the [HOLDS_DATA] role. */
    public operator fun contains(componentId: GearyComponentId): Boolean =
        // Check if contains component or the version with the HOLDS_DATA bit flipped
        componentId in type || componentId.withInvertedRole(HOLDS_DATA) in type

    /** Returns the archetype associated with adding [componentId] to this archetype's [type]. */
    public operator fun plus(componentId: GearyComponentId): Archetype =
        componentAddEdges[componentId] ?: type.let {
            // Ensure that when adding an ID that holds data, we remove the non-data-holding ID
            if (componentId.holdsData() && !componentId.isRelation())
                it.minus(componentId.withoutRole(HOLDS_DATA))
            else it
        }.plus(componentId).getArchetype()

    /** Returns the archetype associated with removing [componentId] to this archetype's [type]. */
    public operator fun minus(componentId: GearyComponentId): Archetype =
        componentRemoveEdges[componentId] ?: type.minus(componentId).getArchetype().also {
            componentRemoveEdges[componentId] = it
        }

    // ==== Entity mutation ====

    /**
     * Adds an entity to this archetype with properly ordered [data].
     *
     * @param data A list of components whose indices correctly match those of this archetype's [dataHoldingType].
     *
     * @return The new [Record] to be associated with this entity from now on.
     */
    @Synchronized
    internal fun addEntityWithData(
        entity: GearyEntity,
        data: List<GearyComponent>
    ): Record {
        ids.add(entity.id.toLong())
        componentData.forEachIndexed { i, compArray ->
            compArray.add(data[i])
        }
        return Record.of(this, size - 1)
    }

    // For the following few functions, both entity and row are passed to avoid doing several array look-ups
    //  (ex when set calls remove).

    /**
     * Add a [component] to an [entity], moving it to the appropriate archetype.
     *
     * @return The new [Record] to be associated with this entity from now on.
     *
     * @see Engine.addComponentFor
     */
    @Synchronized
    internal fun addComponent(
        entity: GearyEntity,
        row: Int,
        component: GearyComponentId
    ): Record? {
        // if already present in this archetype, stop here since we dont need to update any data
        if (contains(component)) return null

        val moveTo = this + (component.withoutRole(HOLDS_DATA))

        val componentData = getComponents(row)
        return moveTo.addEntityWithData(entity, componentData).also { removeEntity(row) }
    }

    /**
     * Sets [data] at a [componentId] for an [entity], moving it to the appropriate archetype.
     * Will remove [componentId] without the [HOLDS_DATA] role if present so an archetype never has both data/no data
     * components at once.
     *
     * @return The new [Record] to be associated with this entity from now on, or null if the [componentId] was already
     * present in this archetype.
     *
     * @see Engine.setComponentFor
     */
    @Synchronized
    internal fun setComponent(
        entity: GearyEntity,
        row: Int,
        componentId: GearyComponentId,
        data: GearyComponent
    ): Record? {
        val isRelation = componentId.isRelation()

        // Relations should not add the HOLDS_DATA bit since the type roles are of the relation's child
        val dataComponent = if (isRelation) componentId else componentId.withRole(HOLDS_DATA)

        //if component was added but not set, remove the old component before adding this one
        val addId = dataComponent.withoutRole(HOLDS_DATA)
        if (addId in type) {
            val removedRecord = removeComponent(entity, row, addId)!!
            return removedRecord.archetype.setComponent(entity, removedRecord.row, dataComponent, data)
        }

        //If component already in this type, just update the data
        val addIndex = indexOf(dataComponent)
        if (addIndex != -1) {
            componentData[addIndex][row] = data
            return null
        }

        val moveTo = this + dataComponent
        val newCompIndex = moveTo.dataHoldingType.indexOf(dataComponent)
        val componentData = getComponents(row).apply {
            add(newCompIndex, data)
        }

        return moveTo.addEntityWithData(entity, componentData).also { removeEntity(row) }
    }

    /**
     * Removes a [component] from an [entity], moving it to the appropriate archetype.
     *
     * @return The new [Record] to be associated with this entity from now on, or null if the [component]
     * was not present in this archetype.
     *
     * @see Engine.removeComponentFor
     */
    @Synchronized
    internal fun removeComponent(
        entity: GearyEntity,
        row: Int,
        component: GearyComponentId
    ): Record? {
        if (component !in type) return null

        val moveTo = this - component

        val componentData = mutableListOf<GearyComponent>()

        val skipData = indexOf(component)
        this.componentData.forEachIndexed { i, it ->
            if (i != skipData)
                componentData.add(it[row])
        }

        removeEntity(row)
        return moveTo.addEntityWithData(entity, componentData)
    }

    /** Gets all the components associated with an entity at a [row]. */
    internal fun getComponents(row: Int): ArrayList<GearyComponent> =
        componentData.mapTo(arrayListOf()) { it[row] }

    /** Removes the entity at a [row] in this archetype, notifying running archetype iterators. */
    @Synchronized
    internal fun removeEntity(row: Int) {
        val replacement = ids.last()
        val lastIndex = ids.lastIndex
        ids[row] = replacement

        if (lastIndex != row)
            componentData.forEach { it[row] = it.last() }

        ids.removeLastOrNull()
        componentData.forEach { it.removeLastOrNull() }

        if (lastIndex != row) {
            runningIterators.forEach {
                it.addMovedRow(lastIndex, row)
            }
            Engine.setRecord(replacement.toGeary(), Record.of(this, row))
        }
    }

    // ==== Event listeners ====

    /** Adds an event [handler] that listens to certain events relating to entities in this archetype. */
    public fun addEventHandler(handler: GearyHandler) {
        _eventHandlers += handler
    }

    public fun addSourceListener(handler: GearyListener) {
        _sourceListeners += handler
    }

    public fun addTargetListener(handler: GearyListener) {
        _targetListeners += handler
    }


    /** Calls an event with data in an [event entity][event]. */
    public fun callEvent(event: GearyEntity, row: Int, source: GearyEntity? = null) {
        val entity = getEntity(row)

        val eventArchetype = event.record.archetype
        val sourceArchetype = source?.record?.archetype

        //TODO performance upgrade will come when we figure out a solution in QueryManager as well.
        for (handler in eventArchetype.eventHandlers) {
            // If an event handler has moved the entity to a new archetype, make sure we follow it.
            val targetRecord = entity.record
            val targetArchetype = entity.record.archetype
            val sourceRecord = source?.record
            val newSourceArchetype = sourceRecord?.archetype
            val newEventRecord = event.record
            val newEventArchetype = newEventRecord.archetype

            // If there's no source but the handler needs a source, skip
            if (source == null && !handler.parentListener.source.isEmpty) continue

            // Check that this handler has a listener associated with it.
            if (!handler.parentListener.target.isEmpty && handler.parentListener !in targetArchetype.targetListeners) continue
            if (newSourceArchetype != null && !handler.parentListener.source.isEmpty && handler.parentListener !in newSourceArchetype.sourceListeners) continue

            // Check that we still match the data if archetype of any involved entities changed.
            if (targetArchetype != this && entity.type !in handler.parentListener.target.family) continue
            if (newEventArchetype != eventArchetype && event.type !in handler.parentListener.event.family) continue
            if (newSourceArchetype != sourceArchetype && event.type !in handler.parentListener.source.family) continue

            val targetScope = RawAccessorDataScope(
                archetype = targetArchetype,
                perArchetypeData = handler.parentListener.target.cacheForArchetype(targetArchetype),
                row = targetRecord.row,
            )
            val eventScope = RawAccessorDataScope(
                archetype = eventArchetype,
                perArchetypeData = handler.parentListener.event.cacheForArchetype(eventArchetype),
                row = newEventRecord.row,
            )
            val sourceScope = if (source == null) null else RawAccessorDataScope(
                archetype = sourceArchetype!!,
                perArchetypeData = handler.parentListener.source.cacheForArchetype(sourceArchetype),
                row = sourceRecord!!.row,
            )
            handler.processAndHandle(sourceScope, targetScope, eventScope)
        }
    }

    // ==== Iterators ====

    /** Stops tracking a running [iterator]. */
    internal fun finalizeIterator(iterator: ArchetypeIterator) {
        runningIterators.remove(iterator)
    }

    /** Creates and tracks an [ArchetypeIterator] for a query. */
    internal fun iteratorFor(query: Query): ArchetypeIterator {
        val iterator = ArchetypeIterator(this, query)
        runningIterators.add(iterator)
        return iterator
    }
}
