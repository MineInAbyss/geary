package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.CompId2ArchetypeMap
import com.mineinabyss.geary.datatypes.maps.Long2ObjectMap
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener

/**
 * Archetypes store a list of entities with the same [EntityType], and provide functions to
 * quickly move them between archetypes.
 *
 * An example use case: If a query matches an archetype, it will also match all entities inside which
 * gives a large performance boost to system iteration.
 */
open class Archetype(
    val type: EntityType,
    val id: Int
) {
    private val records get() = archetypes.records
    private val archetypeProvider get() = archetypes.archetypeProvider
    private val eventRunner get() = archetypes.eventRunner


    val entities: List<Entity> get() = ids.map { it.toGeary() }

    /** The entity ids in this archetype. Indices are the same as [componentData]'s sub-lists. */
    protected val ids: IdList = IdList()

    @PublishedApi
    internal var isIterating: Boolean = false

    /** Component ids in the type that are to hold data */
    // Currently all relations must hold data and the HOLDS_DATA bit on them corresponds to the component part.
    private val dataHoldingType: EntityType = type.filter { it.holdsData() }

    /** An outer list with indices for component ids, and sub-lists with data indexed by entity [ids]. */
    internal val componentData: Array<MutableList<Component>> =
        Array(dataHoldingType.size) { mutableListOf() }

    /** Edges to other archetypes where a single component has been added. */
    internal val componentAddEdges = CompId2ArchetypeMap()

    /** Edges to other archetypes where a single component has been removed. */
    internal val componentRemoveEdges = CompId2ArchetypeMap()

    internal val relations = type.inner.mapNotNull { it.toRelation() }
    internal val relationsWithData = relations.filter { it.id.holdsData() }

    /** Map of relation [Relation.target] id to a list of relations with that [Relation.target]. */
    internal val relationsByTarget: Long2ObjectMap<List<Relation>> = relations
        .groupBy { it.target.toLong() }

    /** Map of relation [Relation.kind] id to a list of relations with that [Relation.kind]. */
    internal val relationsByKind: Long2ObjectMap<List<Relation>> = relations
        .groupBy { it.kind.toLong() }

    /** The amount of entities stored in this archetype. */
    val size: Int get() = ids.size

    private val _sourceListeners = mutableSetOf<Listener>()
    val sourceListeners: Set<Listener> = _sourceListeners

    private val _targetListeners = mutableSetOf<Listener>()
    val targetListeners: Set<Listener> = _targetListeners

    private val _eventListeners = mutableSetOf<Listener>()
    val eventListeners: Set<Listener> = _eventListeners

    // ==== Helper functions ====
    fun getEntity(row: Int): Entity {
        return ids[row].toGeary()
    }

    /**
     * Used to pack data closer together and avoid having hashmaps throughout the archetype.
     *
     * @return The internally used index for this component [id], or 0 if not present. Use contains to check for presence.
     */
    fun indexOf(id: ComponentId): Int = dataHoldingType.indexOf(id)

    /**
     * @return The data under a [componentId] for an entity at [row].
     *
     * @see Record
     */
    operator fun get(row: Int, componentId: ComponentId): Component? {
        val compIndex = indexOf(componentId)
        if (compIndex == -1) return null
        return componentData[compIndex][row]
    }

    /** @return Whether this archetype has a [componentId] in its type. */
    operator fun contains(componentId: ComponentId): Boolean = componentId in type

    /** Returns the archetype associated with adding [componentId] to this archetype's [type]. */
    operator fun plus(componentId: ComponentId): Archetype =
        componentAddEdges[componentId] ?: archetypeProvider.getArchetype(type.plus(componentId)).also {
            componentAddEdges[componentId] = it
        }

    /** Returns the archetype associated with removing [componentId] to this archetype's [type]. */
    operator fun minus(componentId: ComponentId): Archetype =
        componentRemoveEdges[componentId] ?: archetypeProvider.getArchetype(type.minus(componentId)).also {
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
    private fun moveWithNewComponent(
        record: Record,
        newComponent: Component,
        newComponentId: ComponentId,
        entity: Entity,
    ) = move(record, entity) {
        val (oldArc, oldRow) = record
        val newCompIndex = indexOf(newComponentId)

        // Add before new comp
        for (i in 0 until newCompIndex) {
            componentData[i].add(oldArc.componentData[i][oldRow])
        }

        // Add new comp
        componentData[newCompIndex].add(newComponent)

        // Add after new comp
        for (i in newCompIndex + 1..componentData.lastIndex) {
            // Offset by one since this new comp didn't exist
            componentData[i].add(oldArc.componentData[i - 1][oldRow])
        }
    }

    private fun moveOnlyAdding(
        record: Record,
        entity: Entity
    ) = move(record, entity) {
        val (oldArc, oldRow) = record
        for (i in 0..componentData.lastIndex) {
            componentData[i].add(oldArc.componentData[i][oldRow])
        }
    }

    private fun moveWithoutComponent(
        record: Record,
        withoutComponentId: ComponentId,
        entity: Entity,
    ) = move(record, entity) {
        val (oldArc, oldRow) = record
        val withoutCompIndex = indexOf(withoutComponentId)

        // Add before without comp
        for (i in 0 until withoutCompIndex) {
            componentData[i].add(oldArc.componentData[i][oldRow])
        }

        // Add after without comp
        for (i in withoutCompIndex + 1..componentData.lastIndex) {
            componentData[i - 1].add(oldArc.componentData[i][oldRow])
        }
    }

    internal fun createWithoutData(entity: Entity, existingRecord: Record) {
        move(existingRecord, entity) {}
    }

    internal fun createWithoutData(entity: Entity): Record {
        ids.add(entity.id)
        return Record(this, ids.lastIndex)
    }

    internal inline fun move(
        record: Record,
        entity: Entity,
        copyData: () -> Unit
    ) {
        ids.add(entity.id)

        copyData()

        record.row = -1
        record.archetype = this
        record.row = ids.lastIndex
    }

    // For the following few functions, both entity and row are passed to avoid doing several array look-ups
    //  (ex when set calls remove).

    /**
     * Add a [componentId] to an entity represented by [record], moving it to the appropriate archetype.
     *
     * @return Whether the record has changed.
     */
    internal fun addComponent(
        record: Record,
        componentId: ComponentId,
        callEvent: Boolean,
    ): Boolean {
        // if already present in this archetype, stop here since we don't need to update any data
        if (contains(componentId)) return false

        val moveTo = this + (componentId.withoutRole(HOLDS_DATA))

        val entity = record.entity
        val row = record.row
        moveTo.moveOnlyAdding(record, entity)
        removeEntity(row)

        if (callEvent) temporaryEntity { componentAddEvent ->
            componentAddEvent.addRelation(geary.components.addedComponent, componentId, noEvent = true)
            eventRunner.callEvent(record, records[componentAddEvent], null)
        }
        return true
    }

    /**
     * Sets [data] at a [componentId] for an [record], moving it to the appropriate archetype.
     * Will ensure this component without [HOLDS_DATA] is always present.
     *
     * @return Whether the record has changed.
     */
    internal fun setComponent(
        record: Record,
        componentId: ComponentId,
        data: Component,
        callEvent: Boolean,
    ): Boolean {
        val row = record.row
        val dataComponent = componentId.withRole(HOLDS_DATA)

        //If component already in this type, just update the data
        if (contains(dataComponent)) {
            val addIndex = indexOf(dataComponent)
            componentData[addIndex][row] = data
            if (callEvent) temporaryEntity { componentAddEvent ->
                componentAddEvent.addRelation(geary.components.updatedComponent, componentId, noEvent = true)
                eventRunner.callEvent(record, records[componentAddEvent], null)
            }
            return false
        }

        //if component is not already added, add it, then set
        val moveTo =
            if (contains(dataComponent.withoutRole(HOLDS_DATA)))
                this + dataComponent
            else this + dataComponent.withoutRole(HOLDS_DATA) + dataComponent

        val entity = record.entity
        moveTo.moveWithNewComponent(record, data, dataComponent, entity)
        removeEntity(row)

        if (callEvent && targetListeners.isNotEmpty()) {
            // Archetype for the set event
            val eventArc = archetypeProvider.getArchetype(GearyEntityType(ulongArrayOf(Relation.of(geary.components.setComponent, componentId).id)))
            if(eventArc.eventListeners.isNotEmpty()) {
                temporaryEntity { componentAddEvent ->
                    componentAddEvent.addRelation(geary.components.setComponent, componentId, noEvent = true)
                    eventRunner.callEvent(record, records[componentAddEvent], null)
                }
            }
        }
        return true
    }

    /**
     * Removes a [component] from an [record], moving it to the appropriate archetype.
     *
     * @return Whether the record has changed.
     */
    internal fun removeComponent(
        record: Record,
        component: ComponentId
    ): Boolean {
        with(record.archetype) {
            val row = record.row

            if (component !in type) return false

            val moveTo = this - component

            val entity = record.entity
            moveTo.moveWithoutComponent(record, component, entity)
            removeEntity(row)
        }
        return true
    }

    /** Gets all the components associated with an entity at a [row]. */
    internal fun getComponents(row: Int, add: Pair<Component, Int>? = null): Array<Component> {
        if (add != null) {
            val arr = Array<Any?>(componentData.size + 1) { null }
            val (addElement, addIndex) = add
            for (i in 0 until addIndex) arr[i] = componentData[i][row]
            arr[addIndex] = addElement
            for (i in addIndex..componentData.lastIndex) arr[i + 1] = componentData[i][row]
            @Suppress("UNCHECKED_CAST") // For loop above ensures no nulls
            return arr as Array<Component>
        } else
            return Array(componentData.size) { i: Int -> componentData[i][row] }
    }

    /**
     * Queries for specific relations or by kind/target.
     *
     * When [kind] or [target] are the [Any] component, matches against any relation.
     * Both [kind] and [target] cannot be [Any].
     *
     * The if a parameter is the [Any] component, the [HOLDS_DATA] role indicates whether other components
     * matched must also hold data themselves.
     * All other roles are ignored for the [target].
     */
    internal fun getRelations(kind: ComponentId, target: EntityId): List<Relation> {
        val specificKind = kind and ENTITY_MASK != geary.components.any
        val specificTarget = target and ENTITY_MASK != geary.components.any
        return when {
            specificKind && specificTarget -> listOf(Relation.of(kind, target))
            specificTarget -> relationsByTarget[target.toLong()]
            specificKind -> relationsByKind[kind.toLong()]
            else -> relations
        }?.run { //TODO this technically doesnt need to run when specificKind is set
            if (kind.hasRole(HOLDS_DATA)) filter { it.hasRole(HOLDS_DATA) } else this
        }?.run {
            if (target.holdsData()) filter { it.target.withRole(HOLDS_DATA) in type } else this
        } ?: emptyList()
    }

    /**
     * Removes the entity at a [row] in this archetype, notifying running archetype iterators.
     *
     * Must be run synchronously.
     */
    fun removeEntity(row: Int) {
        val lastIndex = ids.lastIndex

        // Move entity in last row to deleted row
        if (lastIndex != row) {
            val replacement = ids[lastIndex]
            ids[row] = replacement
            componentData.forEach { it[row] = it.last() }
            records[replacement.toGeary()].apply {
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
    fun addEventHandler(handler: Listener) {
        _eventListeners += handler
    }

    fun addSourceListener(handler: Listener) {
        _sourceListeners += handler
    }

    fun addTargetListener(handler: Listener) {
        _targetListeners += handler
    }

    // TODO upto is a bad approach if a system both adds and removes entities?
    inline fun forEach(upTo: Int, crossinline run: (EntityId) -> Unit) {
        var row = 0
        while (row < size && row <= upTo) {
            run(getEntity(row).id)
            row++
        }
    }
}
