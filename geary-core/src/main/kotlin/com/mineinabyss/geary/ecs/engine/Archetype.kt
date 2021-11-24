package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.events.ComponentAddEvent
import com.mineinabyss.geary.ecs.query.Query
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongArrayList
import java.util.*
import kotlin.reflect.KClass

/**
 * Archetypes store a list of entities with the same [GearyType], and provide functions to
 * quickly move them between archetypes.
 *
 * An example use case: If a query matches an archetype, it will also match all entities inside which
 * gives a large performance boost to system iteration.
 */
public data class Archetype(
    public val type: GearyType,
) {
    /** The entity ids in this archetype. Indices are the same as [componentData]'s sub-lists. */
    //TODO aim to make private
    internal val ids: LongArrayList = LongArrayList()

    /** Component ids in the type that are to hold data */
    // Currently all relations must hold data and the HOLDS_DATA bit on them corresponds to the component part.
    private val dataHoldingType = type.filter { it.holdsData() || it.isRelation() }

    /** An outer list with indices for component ids, and sub-lists with data indexed by entity [ids]. */
    internal val componentData: List<MutableList<GearyComponent>> = dataHoldingType.map { mutableListOf() }

    /** Edges to other archetypes where a single component has been added. */
    internal val componentAddEdges = Long2ObjectOpenHashMap<Archetype>()

    /** Edges to other archetypes where a single component has been removed. */
    internal val componentRemoveEdges = Long2ObjectOpenHashMap<Archetype>()

    /** Map of relation parent id to a list of relations with that parent. */
    internal val relations: Long2ObjectOpenHashMap<List<Relation>> = type
        .mapNotNull { it.toRelation() }
        .groupBy { it.data.id.toLong() }
        .let { Long2ObjectOpenHashMap(it) }

    /** A map of a relation's data type id to full relations that store data of that type. */
    internal val dataHoldingRelations: Long2ObjectOpenHashMap<List<Relation>> by lazy {
        val map = Long2ObjectOpenHashMap<List<Relation>>()
        relations.forEach { (key, value) ->
            val dataHolding = value.filter { it.key.holdsData() }
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

    /** A map of event class type to a set of event handlers which fire on that event. */
    private val listeners = mutableMapOf<KClass<*>, MutableSet<GearyEventHandler<*>>>()

    /**
     * Sets of event handlers in an outer array whose indices represent internal component ids.
     *
     * After an entity gets added to this archetype, the appropriate handlers fire based on the component added.
     */
    //FIXME this should be used for the full type not just data holding.
    private val componentAddListeners = Array(dataHoldingType.size) { mutableSetOf<GearyEventHandler<*>>() }

    // Basically just want a weak set where stuff gets auto removed when it is no longer running
    // We put our iterator and null and this WeakHashMap handles the rest for us.
    private val runningIterators = Collections.newSetFromMap(WeakHashMap<ArchetypeIterator, Boolean>())

    // ==== Helper functions ====

    /** @return This Archetype's [relations] that are also a part of [matchRelations]. */
    public fun matchedRelationsFor(matchRelations: Collection<RelationDataType>): Map<RelationDataType, List<Relation>> =
        matchRelations
            .filter { it.id.toLong() in relations }
            .associateWith { relations[it.id.toLong()]!! } //TODO handle null error

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
        entity: GearyEntityId,
        data: List<GearyComponent>
    ): Record {
        ids.add(entity.toLong())
        componentData.forEachIndexed { i, compArray ->
            compArray.add(data[i])
        }
        return Record(this, size - 1)
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
        entity: GearyEntityId,
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
        entity: GearyEntityId,
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
        entity: GearyEntityId,
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
            Engine.setRecord(replacement.toULong(), Record(this, row))
        }
    }

    // ==== Event listeners ====

    /** Adds [handler] that fires for a class [forClass]. */
    public fun <T : Any> addEventHandler(
        forClass: KClass<T>,
        handler: GearyEventHandler<T>
    ) {
        when (forClass) {
            ComponentAddEvent::class -> handler.holder.family.components
                .map { indexOf(it) }
                .forEach { componentAddListeners[it] += handler }
            else -> listeners.getOrPut(forClass) { mutableSetOf() } += handler
        }
    }

    /** Calls an event with type [T] on an entity at [row], providing it with some [eventData]. */
    public inline fun <reified T : Any> callEvent(eventData: T, row: Int) {
        callEvent(T::class, eventData, row)
    }

    /** Calls an event with type [kClass] on an entity at [row], providing it with some [eventData]. */
    public fun <T : Any> callEvent(kClass: KClass<T>, eventData: T, row: Int) {
        val entity = getEntity(row)

        when (eventData) {
            is ComponentAddEvent -> {
                val index = indexOf(eventData.component)
                if (index == -1) return
                componentAddListeners[index].forEach {
                    val scope = RawAccessorDataScope(this, it.holder.cacheForArchetype(this), row, entity)
                    it.runEvent(eventData, scope)
                }
            }
            else -> listeners[kClass]?.forEach {
                //TODO clean up by moving into runEvent
                val scope = RawAccessorDataScope(this, it.holder.cacheForArchetype(this), row, entity)
                it.runEvent(eventData, scope)
            }
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
