package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.CompId2ArchetypeMap
import com.mineinabyss.geary.helpers.temporaryEntity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.RelationWithData

/**
 * Archetypes store a list of entities with the same [EntityType], and provide functions to
 * quickly move them between archetypes.
 *
 * An example use case: If a query matches an archetype, it will also match all entities inside which
 * gives a large performance boost to system iteration.
 */
class Archetype internal constructor(
    val type: EntityType,
    var id: Int
) {
    private val records get() = archetypes.records
    private val archetypeProvider get() = archetypes.archetypeProvider
    private val eventRunner get() = archetypes.eventRunner

    val entities: Sequence<Entity> get() = ids.getEntities()

    /** The entity ids in this archetype. Indices are the same as [componentData]'s sub-lists. */
    private val ids: IdList = IdList()

    @PublishedApi
    internal var isIterating: Boolean = false

    private var unregistered: Boolean = false

    private var allowUnregister: Boolean? = null

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

    internal val relations: EntityType = type.filter { it.isRelation() }
    internal val relationsWithData: EntityType = relations.filter { it.holdsData() }

    fun getRelationsByTarget(target: EntityId): List<Relation> {
        return relations.filter { Relation.of(it).target.toLong() == target.toLong() }.map { Relation.of(it) }
    }

    fun getRelationsByKind(kind: ComponentId): List<Relation> {
        return relations.filter { Relation.of(it).kind.toLong() == kind.toLong() }.map { Relation.of(it) }
    }

    /** The amount of entities stored in this archetype. */
    val size: Int get() = ids.size

    val sourceListeners = mutableSetOf<Listener>()

    val targetListeners = mutableSetOf<Listener>()

    val eventListeners = mutableSetOf<Listener>()

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
    operator fun plus(componentId: ComponentId): Archetype {
        componentAddEdges[componentId]?.let { return it }
        if (componentId.holdsData()) {
            // Try to get via the component without the data role
            componentAddEdges[componentId.withoutRole(HOLDS_DATA)]
                ?.plus(componentId)?.let { return it }
            if (componentId.withoutRole(HOLDS_DATA) !in type)
                return this + componentId.withoutRole(HOLDS_DATA) + componentId
        }
        val archetype = archetypeProvider.getArchetype(type + componentId)
        componentAddEdges[componentId] = archetype
        archetype.componentRemoveEdges[componentId] = this
        return archetype
    }

    /** Returns the archetype associated with removing [componentId] to this archetype's [type]. */
    operator fun minus(componentId: ComponentId): Archetype =
        componentRemoveEdges.getOrSet(componentId) {
            archetypeProvider.getArchetype(type.minus(componentId))
                .also { it.componentAddEdges[componentId] = this }
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
        entity: EntityId,
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
        entity: EntityId
    ) = move(record, entity) {
        val (oldArc, oldRow) = record
        for (i in 0..componentData.lastIndex) {
            componentData[i].add(oldArc.componentData[i][oldRow])
        }
    }

    private fun moveWithoutComponent(
        record: Record,
        withoutComponentId: ComponentId,
        entity: EntityId,
    ) = move(record, entity) {
        val (oldArc, oldRow) = record
        val withoutCompIndex = oldArc.indexOf(withoutComponentId)

        // If removing a component that's added and not set, we just copy all data
        if (withoutCompIndex == -1) {
            for (i in 0..componentData.lastIndex) {
                componentData[i].add(oldArc.componentData[i][oldRow])
            }
            return@move
        }

        // Add before without comp
        for (i in 0 until withoutCompIndex) {
            componentData[i].add(oldArc.componentData[i][oldRow])
        }

        // Add after without comp
        for (i in withoutCompIndex + 1..oldArc.componentData.lastIndex) {
            componentData[i - 1].add(oldArc.componentData[i][oldRow])
        }
    }

    internal fun createWithoutData(entity: Entity, existingRecord: Record) {
        move(existingRecord, entity.id) {}
    }

    internal fun createWithoutData(entity: Entity): Record {
        ids.add(entity.id)
        return Record(this, ids.lastIndex)
    }

    internal inline fun move(
        record: Record,
        entity: EntityId,
        copyData: () -> Unit
    ) {
        if (unregistered) error("Tried adding entity to archetype that is no longer registered. Was it referenced outside of Geary?")
        ids.add(entity)

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

        val row = record.row
        val entityId = ids[row]
        moveTo.moveOnlyAdding(record, entityId)
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
        val addIndex = indexOf(dataComponent)
        if (addIndex != -1) {
            componentData[addIndex][row] = data
            if (callEvent) temporaryEntity { componentAddEvent ->
                componentAddEvent.addRelation(geary.components.updatedComponent, componentId, noEvent = true)
                componentAddEvent.add(geary.components.keepArchetype, noEvent = true)
                eventRunner.callEvent(record, records[componentAddEvent], null)
            }
            return false
        }

        //if component is not already added, add it, then set
        val entityId = ids[row]
        val moveTo = this + dataComponent
        moveTo.moveWithNewComponent(record, data, dataComponent, entityId)
        removeEntity(row)

        if (callEvent && moveTo.targetListeners.isNotEmpty()) {
            // Archetype for the set event
            val eventArc = archetypeProvider.getArchetype(
                GearyEntityType(
                    ulongArrayOf(
                        Relation.of(
                            geary.components.setComponent,
                            componentId
                        ).id
                    )
                )
            )
            if (eventArc.eventListeners.isNotEmpty()) {
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
            val entityId = ids[row]

            if (component !in type) return false

            val moveTo = this - component

            moveTo.moveWithoutComponent(record, component, entityId)
            removeEntity(row)
        }
        return true
    }

    private fun unregisterIfEmpty() {
        if (allowUnregister == false) return
        if (type.size != 0 && ids.size == 0 && componentAddEdges.size == 0) {
            if (allowUnregister == null) allowUnregister = type.contains(geary.components.keepArchetype)
            if (allowUnregister == false) return
            archetypes.queryManager.unregisterArchetype(this)
            unregistered = true
            for ((id, archetype) in componentRemoveEdges.entries()) {
                archetype.componentAddEdges.remove(id)
                archetype.unregisterIfEmpty()
            }
            componentRemoveEdges.clear()
            targetListeners.clear()
            sourceListeners.clear()
            eventListeners.clear()
        }
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
            specificTarget -> getRelationsByTarget(target)
            specificKind -> getRelationsByKind(kind)
            else -> relations.map { Relation.of(it) }
        }.run { //TODO this technically doesnt need to run when specificKind is set
            if (kind.hasRole(HOLDS_DATA)) filter { it.hasRole(HOLDS_DATA) } else this
        }.run {
            if (target.holdsData()) filter { it.target.withRole(HOLDS_DATA) in type } else this
        }
    }

    internal fun readRelationDataFor(
        row: Int,
        kind: ComponentId,
        target: EntityId,
        relations: List<Relation>
    ): List<RelationWithData<*, *>> {
        return relations.map { relation ->
            RelationWithData(
                data = if (kind.hasRole(HOLDS_DATA)) this[row, relation.id] else null,
                targetData = if (target.hasRole(HOLDS_DATA)) this[row, relation.target.withRole(HOLDS_DATA)] else null,
                relation = relation
            )
        }
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

        // If we're the last archetype in the chain with no entities, unregister to free up memory
        unregisterIfEmpty()
    }

    override fun equals(other: Any?): Boolean {
        return type == (other as? Archetype)?.type
    }
}
