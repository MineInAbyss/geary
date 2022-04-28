package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.CompId2ArchetypeMap
import com.mineinabyss.geary.datatypes.maps.Long2ObjectMap
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.GearyEngine
import com.mineinabyss.geary.events.GearyHandler
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope
import com.mineinabyss.geary.systems.query.GearyQuery
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Archetypes store a list of entities with the same [GearyType], and provide functions to
 * quickly move them between archetypes.
 *
 * An example use case: If a query matches an archetype, it will also match all entities inside which
 * gives a large performance boost to system iteration.
 */
public data class Archetype(
    private val engine: Engine,
    public val type: GearyType,
    public val id: Int
) {
    /** A mutex for anything which needs the size of ids to remain unchanged. */
    private val entityAddition = SynchronizedObject()

    /** The entity ids in this archetype. Indices are the same as [componentData]'s sub-lists. */
    //TODO aim to make private
    internal val ids: IdList = IdList()
    private val queuedRemoval = mutableListOf<Int>()
    private val queueRemoval = SynchronizedObject()

    internal var isIterating = false

    /** Component ids in the type that are to hold data */
    // Currently all relations must hold data and the HOLDS_DATA bit on them corresponds to the component part.
    private val dataHoldingType = type.filter { it.holdsData() || it.isRelation() }

    /** An outer list with indices for component ids, and sub-lists with data indexed by entity [ids]. */
    internal val componentData: List<MutableList<GearyComponent>> =
        dataHoldingType.map { mutableListOf() }

    /** Edges to other archetypes where a single component has been added. */
    internal val componentAddEdges = CompId2ArchetypeMap(engine)

    /** Edges to other archetypes where a single component has been removed. */
    internal val componentRemoveEdges = CompId2ArchetypeMap(engine)

    internal val relations = type.inner.mapNotNull { it.toRelation() }

    /** Map of relation [Relation.value] id to a list of relations with that [Relation.value]. */
    internal val relationsByValue: Long2ObjectMap<List<Relation>> = relations
        .groupBy { it.value.id.toLong() }

    /** Map of relation [Relation.key] id to a list of relations with that [Relation.key]. */
    internal val relationsByKey: Long2ObjectMap<List<Relation>> = relations
        .groupBy { it.key.toLong() }

    /** A map of a relation's data type id to full relations that store data of that type. */
    internal val dataHoldingRelations: Long2ObjectMap<List<Relation>> by lazy {
        val map = mutableMapOf<Long, List<Relation>>()
        relationsByValue.forEach { (key, values) ->
            val dataHolding = values.filter { it.key.holdsData() }
            if (dataHolding.isNotEmpty()) map[key] = dataHolding
        }
        map
    }


    /** A map of component ids to index used internally in this archetype (ex. in [componentData])*/
    private val comp2indices = mutableMapOf<Long, Int>().apply {
        dataHoldingType.forEachIndexed { i, compId -> put(compId.toLong(), i) }
//        defaultReturnValue(-1)
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

    // ==== Helper functions ====
    public fun getEntity(row: Int): GearyEntity {
        return ids[row].toGeary()
    }

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
    internal fun indexOf(id: GearyComponentId): Int = comp2indices[id.toLong()] ?: -1

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


    /** @return Whether this archetype has a [componentId] in its type, regardless of the [HOLDS_DATA] role. */
    public operator fun contains(componentId: GearyComponentId): Boolean =
        // Check if contains component or the version with the HOLDS_DATA bit flipped
        componentId in type || componentId.withInvertedRole(HOLDS_DATA) in type

    /** Returns the archetype associated with adding [componentId] to this archetype's [type]. */
    public operator fun plus(componentId: GearyComponentId): Archetype =
        if (componentId in componentAddEdges)
            componentAddEdges[componentId]
        else
            engine.getArchetype(
                type.let {
                    // Ensure that when adding an ID that holds data, we remove the non-data-holding ID
                    if (componentId.holdsData() && !componentId.isRelation())
                        it.minus(componentId.withoutRole(HOLDS_DATA))
                    else it
                }.plus(componentId)
            )

    /** Returns the archetype associated with removing [componentId] to this archetype's [type]. */
    public operator fun minus(componentId: GearyComponentId): Archetype =
        if (componentId in componentRemoveEdges)
            componentRemoveEdges[componentId]
        else engine.getArchetype(type.minus(componentId)).also {
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
    internal fun addEntityWithData(
        record: Record,
        data: Array<GearyComponent>,
        entity: GearyEntity = record.entity,
    ) = synchronized(entityAddition) {
        synchronized(record) {
            ids.add(entity.id.toLong())
            componentData.forEachIndexed { i, compArray ->
                compArray.add(data[i])
            }
            record.row = -1
            record.archetype = this
            record.row = ids.lastIndex
        }
    }

    // For the following few functions, both entity and row are passed to avoid doing several array look-ups
    //  (ex when set calls remove).

    /**
     * Add a [component] to an entity represented by [record], moving it to the appropriate archetype.
     *
     * @return Whether the record has changed.
     *
     * @see Engine.addComponentFor
     */
    internal fun addComponent(
        record: Record,
        component: GearyComponentId
    ): Boolean {
        // if already present in this archetype, stop here since we dont need to update any data
        if (contains(component)) return false

        val moveTo = this + (component.withoutRole(HOLDS_DATA))

        val componentData = getComponents(record.row)
        scheduleRemoveRow(record.row)
        moveTo.addEntityWithData(record, componentData)
        return true
    }

    /**
     * Sets [data] at a [componentId] for an [record], moving it to the appropriate archetype.
     * Will remove [componentId] without the [HOLDS_DATA] role if present so an archetype never has both data/no data
     * components at once.
     *
     * @return The new [Record] to be associated with this entity from now on, or null if the [componentId] was already
     * present in this archetype.
     *
     * @see Engine.setComponentFor
     */
    internal fun setComponent(
        record: Record,
        componentId: GearyComponentId,
        data: GearyComponent
    ): Boolean {
        val row = record.row
        val isRelation = componentId.isRelation()

        // Relations should not add the HOLDS_DATA bit since the type roles are of the relation's child
        val dataComponent = if (isRelation) componentId else componentId.withRole(HOLDS_DATA)


        //If component already in this type, just update the data
        val addIndex = indexOf(dataComponent)
        if (addIndex != -1) {
            componentData[addIndex][row] = data
            return false
        }

        //if component was added but not set, remove the added component before setting this one
        val addId = dataComponent.withoutRole(HOLDS_DATA)

        if (addId in type) {
            removeComponent(record, addId)
            record.archetype.setComponent(record, dataComponent, data)
        }


        val moveTo = this + dataComponent
        val newCompIndex = moveTo.dataHoldingType.indexOf(dataComponent)
        val componentData = getComponents(row, add = data to newCompIndex)

        scheduleRemoveRow(row)
        moveTo.addEntityWithData(record, componentData)
        return true
    }

    /**
     * Removes a [component] from an [record], moving it to the appropriate archetype.
     *
     * @return Whether the record has changed.
     *
     * @see Engine.removeComponentFor
     */
    internal fun removeComponent(
        record: Record,
        component: GearyComponentId
    ): Boolean = synchronized(record) { //TODO find a proper library for this or make async
        with(record.archetype) {
            val row = record.row

            if (component !in type) return@synchronized false

            val moveTo = this - component

            val skipData = indexOf(component)
            val copiedData =
                if (component.holdsData())
                    (Array<Any>((componentData.size - 1).coerceAtLeast(0)) {}).also { data ->
                        for (i in 0 until skipData) data[i] = componentData[i][row]
                        for (i in (skipData + 1)..componentData.lastIndex) data[i - 1] = componentData[i][row]
                    }
                else (Array<Any>(componentData.size) {}).also { data ->
                    for (i in 0..componentData.lastIndex) data[i] = componentData[i][row]
                }
            moveTo.addEntityWithData(record, copiedData)
            scheduleRemoveRow(row)
        }
        return@synchronized true
    }

    /** Gets all the components associated with an entity at a [row]. */
    internal fun getComponents(row: Int, add: Pair<GearyComponent, Int>? = null): Array<GearyComponent> {
        if (add != null) {
            val arr = Array<Any?>(componentData.size + 1) { null }
            val (addElement, addIndex) = add
            for (i in 0 until addIndex) arr[i] = componentData[i][row]
            arr[addIndex] = addElement
            for (i in addIndex..componentData.lastIndex) arr[i + 1] = componentData[i][row]
            @Suppress("UNCHECKED_CAST") // For loop above ensures no nulls
            return arr as Array<GearyComponent>
        } else
            return Array(componentData.size) { i: Int -> componentData[i][row] }
    }

    internal fun scheduleRemoveRow(row: Int) {
        synchronized(queueRemoval) {
            queuedRemoval.add(row)
        }
        //TODO another variable, is scheduled so we dont do a hashmap lookup each time
        if (!isIterating) (engine as GearyEngine).scheduleRemove(this)
    }

    /**
     * Removes the entity at a [row] in this archetype, notifying running archetype iterators.
     *
     * Must be run synchronously.
     */
    private fun removeEntity(row: Int) {
        val lastIndex = ids.lastIndex

        // Move entity in last row to deleted row
        if (lastIndex != row) {
            val replacement = ids[lastIndex]
            ids[row] = replacement
            componentData.forEach { it[row] = it.last() }
            engine.getRecord(replacement.toGeary()).apply {
                this.archetype = this@Archetype
                this.row = row
            }
        }

        // Delete last row's data
        ids.removeLastOrNull()
        componentData.forEach { it.removeLastOrNull() }
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
    public fun callEvent(
        event: GearyEntity,
        row: Int,
        source: GearyEntity? = null,
    ) {
        val target = getEntity(row)
        val engine = (engine as GearyEngine) //TODO expose properly for internal api

        val types = engine.typeMap
        // Lock access to entities involved

        val origEventArc = types.get(event).archetype
        val origSourceArc = source?.let { types.get(it) }?.archetype

        //TODO performance upgrade will come when we figure out a solution in QueryManager as well.
        for (handler in origEventArc.eventHandlers) {
            // If an event handler has moved the entity to a new archetype, make sure we follow it.
            val (targetArc, targetRow) = types.get(target)
            val (eventArc, eventRow) = types.get(event)
            val sourceRecord = source?.let { types.get(it) }
            val sourceArc = sourceRecord?.archetype
            val sourceRow = sourceRecord?.row

            // If there's no source but the handler needs a source, skip
            if (source == null && !handler.parentListener.source.isEmpty) continue

            // Check that this handler has a listener associated with it.
            if (!handler.parentListener.target.isEmpty && handler.parentListener !in targetArc.targetListeners) continue
            if (sourceArc != null && !handler.parentListener.source.isEmpty && handler.parentListener !in sourceArc.sourceListeners) continue

            // Check that we still match the data if archetype of any involved entities changed.
            if (targetArc != this@Archetype && targetArc.type !in handler.parentListener.target.family) continue
            if (eventArc != origEventArc && eventArc.type !in handler.parentListener.event.family) continue
            if (sourceArc != origSourceArc && eventArc.type !in handler.parentListener.source.family) continue

            val listenerName = handler.parentListener::class.simpleName
            val targetScope = runCatching {
                RawAccessorDataScope(
                    archetype = targetArc,
                    perArchetypeData = handler.parentListener.target.cacheForArchetype(targetArc),
                    row = targetRow,
                )
            }.getOrElse { throw IllegalStateException("Failed while reading target scope on $listenerName", it) }
            val eventScope = runCatching {
                RawAccessorDataScope(
                    archetype = eventArc,
                    perArchetypeData = handler.parentListener.event.cacheForArchetype(eventArc),
                    row = eventRow,
                )
            }.getOrElse { throw IllegalStateException("Failed while reading event scope on $listenerName", it) }
            val sourceScope = if (sourceRecord == null) null else runCatching {
                RawAccessorDataScope(
                    archetype = sourceArc!!,
                    perArchetypeData = handler.parentListener.source.cacheForArchetype(sourceArc),
                    row = sourceRow!!,
                )
            }.getOrElse { throw IllegalStateException("Failed while reading source scope on $listenerName", it) }
            handler.processAndHandle(sourceScope, targetScope, eventScope)
        }
    }

    // ==== Iterators ====

//    /** Stops tracking a running [iterator]. */
//    internal fun finalizeIterator(iterator: ArchetypeIterator) {
//        runningIterators.remove(iterator)
//    }

    /** Creates and tracks an [ArchetypeIterator] for a query. */
    internal fun iteratorFor(query: GearyQuery): ArchetypeIterator {
        val iterator = ArchetypeIterator(this, query)
        return iterator
    }

    /** Removes any queued up entity deletions. */
    internal fun cleanup() {
        synchronized(queueRemoval) {
            if (!isIterating)
                queuedRemoval.sort()
            // Since the rows were added in order while iterating, the list is always sorted,
            // so we don't worry about moving rows
            while (queuedRemoval.isNotEmpty()) {
                val last = queuedRemoval.removeLast()
                removeEntity(last)
            }
        }
    }
}
