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
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

private typealias IdList = LongArrayList

///** @return The entity stored at a given [row] in this archetype. */
//internal fun IdList.getEntity(row: Int): GearyEntity = getLong(row).toGeary()

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

    internal var iterationJob: Job? = null

    /** A mutex for anything which needs the size of ids to remain unchanged. */
    private val entityAddition = Mutex()

    /** The entity ids in this archetype. Indices are the same as [componentData]'s sub-lists. */
    //TODO aim to make private
    internal val ids: IdList = IdList()

    /** Component ids in the type that are to hold data */
    // Currently all relations must hold data and the HOLDS_DATA bit on them corresponds to the component part.
    private val dataHoldingType = type.filter { it.holdsData() || it.isRelation() }

    /** An outer list with indices for component ids, and sub-lists with data indexed by entity [ids]. */
    internal val componentData: List<MutableList<GearyComponent>> =
        dataHoldingType.inner.map { mutableListOf() }

    /** Edges to other archetypes where a single component has been added. */
    internal val componentAddEdges = CompId2ArchetypeMap(engine)

    /** Edges to other archetypes where a single component has been removed. */
    internal val componentRemoveEdges = CompId2ArchetypeMap(engine)

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
    public suspend fun awaitIteration() {
        iterationJob?.join()
    }

    public fun getEntity(row: Int): GearyEntity {
        return ids.getLong(row).toGeary()
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


    /** @return Whether this archetype has a [componentId] in its type, regardless of the [HOLDS_DATA] role. */
    public operator fun contains(componentId: GearyComponentId): Boolean =
        // Check if contains component or the version with the HOLDS_DATA bit flipped
        componentId in type || componentId.withInvertedRole(HOLDS_DATA) in type

    /** Returns the archetype associated with adding [componentId] to this archetype's [type]. */
    @Synchronized
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
    @Synchronized
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
    //TODO proper async add
    internal suspend fun addEntityWithData(
        entity: GearyEntity,
        data: List<GearyComponent>
    ): Record = entityAddition.withLock {
//        awaitIteration()
        ids.add(entity.id.toLong())
        componentData.forEachIndexed { i, compArray ->
            compArray.add(data[i])
        }
        Record.of(this, ids.lastIndex)
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
    internal suspend fun addComponent(
        entity: GearyEntity,
        row: Int,
        component: GearyComponentId
    ): Record? {
        // if already present in this archetype, stop here since we dont need to update any data
        if (contains(component)) return null

        val moveTo = this + (component.withoutRole(HOLDS_DATA))

        val componentData = getComponents(row)
        removeEntity(row)
        return moveTo.addEntityWithData(entity, componentData)
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
    internal suspend fun setComponent(
        entity: GearyEntity,
        row: Int,
        componentId: GearyComponentId,
        data: GearyComponent
    ): Record? {
        val isRelation = componentId.isRelation()

        // Relations should not add the HOLDS_DATA bit since the type roles are of the relation's child
        val dataComponent = if (isRelation) componentId else componentId.withRole(HOLDS_DATA)


        //If component already in this type, just update the data
        val addIndex = indexOf(dataComponent)
        if (addIndex != -1) {
            componentData[addIndex][row] = data
            return null
        }

        //if component was added but not set, remove the added component before setting this one
        val addId = dataComponent.withoutRole(HOLDS_DATA)

        if (addId in type) {
            //TODO ensure this is safe while archetype is iterating
            val removedRecord = removeComponent(entity, row, addId)!!
            return removedRecord.archetype.setComponent(entity, removedRecord.row, dataComponent, data)
        }


        val moveTo = this + dataComponent
        val newCompIndex = moveTo.dataHoldingType.indexOf(dataComponent)
        val componentData = getComponents(row).apply { add(newCompIndex, data) }

        removeEntity(row)
        return moveTo.addEntityWithData(entity, componentData)
    }

    /**
     * Removes a [component] from an [entity], moving it to the appropriate archetype.
     *
     * @return The new [Record] to be associated with this entity from now on, or null if the [component]
     * was not present in this archetype.
     *
     * @see Engine.removeComponentFor
     */
    internal suspend fun removeComponent(
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
    internal suspend fun removeEntity(row: Int) {
        while (true) {
//            awaitIteration()
            val lastIndex = ids.lastIndex
            val lastEntity = ids.getLong(lastIndex).toGeary()
            if (lastIndex != row) engine.lock(lastEntity)

            entityAddition.lock()
            if (ids.lastIndex != lastIndex) {
                entityAddition.unlock()
                if (lastIndex != row) engine.unlock(lastEntity)
                continue
            }
            val replacement = ids.getLong(lastIndex)
            ids[row] = replacement

            // Move entity in last row to deleted row
            if (lastIndex != row) {
                componentData.forEach { it[row] = it.last() }
                runningIterators.forEach {
                    it.addMovedRow(lastIndex, row)
                }
                engine.setRecord(replacement.toGeary(), Record.of(this@Archetype, row))
            }

            // Delete last row's data
            ids.removeLastOrNull()
            componentData.forEach { it.removeLastOrNull() }

            if (lastIndex != row) engine.unlock(lastEntity)
            entityAddition.unlock()
            return
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
    public suspend fun callEvent(event: GearyEntity, row: Int, source: GearyEntity? = null) {
        callEvent(event, row, source, true)
    }

    internal suspend fun callEvent(event: GearyEntity, row: Int, source: GearyEntity? = null, lock: Boolean) {
        val target = getEntity(row)
        val engine = (engine as GearyEngine) //TODO expose properly for internal api

        val types = engine.typeMap
        // Lock access to entities involved
        val sMutex = source?.let { types.getMutex(it) }
        val tMutex = types.getMutex(target)
        val eMutex = types.getMutex(event)
        if (lock) {
            sMutex?.lock()
            tMutex.lock()
            eMutex.lock()
        }

        val origEventArc = types.unsafeGet(event).archetype
        val origSourceArc = source?.let { types.unsafeGet(it) }?.archetype

        //TODO performance upgrade will come when we figure out a solution in QueryManager as well.
        for (handler in origEventArc.eventHandlers) {
            // If an event handler has moved the entity to a new archetype, make sure we follow it.
            val (targetArc, targetRow) = types.unsafeGet(target)
            val (eventArc, eventRow) = types.unsafeGet(event)
            val sourceRecord = source?.let { types.unsafeGet(it) }
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
        if (lock) {
            sMutex?.unlock()
            tMutex.unlock()
            eMutex.unlock()
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
