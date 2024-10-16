package com.mineinabyss.geary.engine.archetypes

import androidx.collection.*
import com.mineinabyss.geary.components.ReservedComponents
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.observers.events.*
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
    var id: Int,
    private val records: ArrayTypeMap,
    private val archetypeProvider: ArchetypeProvider,
) {
    val entities: EntityIdArray
        get() = ULongArray(size) { id -> ids[id].toULong() }

    /** The entity ids in this archetype. Indices are the same as [componentData]'s sub-lists. */
    private val ids = mutableLongListOf()

    @PublishedApi
    internal var isIterating: Boolean = false

    private var unregistered: Boolean = false

    // This is way slower as a Boolean? because of boxing
    private var allowUnregister: Byte = 0
    internal var indexInRecords = -1

    /** Component ids in the type that are to hold data */
    // Currently all relations must hold data and the HOLDS_DATA bit on them corresponds to the component part.
    internal val dataHoldingType: EntityType = type.filter { it.holdsData() }

    /** An outer list with indices for component ids, and sub-lists with data indexed by entity [ids]. */
    internal val componentData: Array<MutableObjectList<Component>> =
        Array(dataHoldingType.size) { mutableObjectListOf() }

    /** Edges to other archetypes where a single component has been added. */
    internal val componentAddEdges = LongSparseArray<Archetype>()

    /** Edges to other archetypes where a single component has been removed. */
    internal val componentRemoveEdges = LongSparseArray<Archetype>()

    internal val relations: EntityType = type.filter { it.isRelation() }
    internal val relationsWithData: EntityType = relations.filter { it.holdsData() }

    fun getRelationsByTarget(target: EntityId): EntityType {
        return relations.filter { Relation.of(it).target.toLong() == target.toLong() }
    }

    fun getRelationsByKind(kind: ComponentId): EntityType {
        return relations.filter { Relation.of(it).kind.toLong() == kind.toLong() }
    }

    /** The amount of entities stored in this archetype. */
    val size: Int get() = ids.size

    // ==== Helper functions ====
    fun getEntity(row: Int): EntityId {
        return ids[row].toULong()
    }

    /**
     * Used to pack data closer together and avoid having hashmaps throughout the archetype.
     *
     * @return The internally used index for this component [id], or 0 if not present. Use contains to check for presence.
     */
    fun indexOf(id: ComponentId): Int = dataHoldingType.indexOf(id)

    /**
     * @return The data under a [componentId] for an entity at [row].
     */
    operator fun get(row: Int, componentId: ComponentId): Component? {
        val compIndex = indexOf(componentId)
        if (compIndex < 0) return null
        return componentData[compIndex][row]
    }

    internal fun getUnsafe(row: Int, componentId: ComponentId): Component {
        val compIndex = indexOf(componentId)
        return componentData[compIndex][row]
    }

    /** @return Whether this archetype has a [componentId] in its type. */
    operator fun contains(componentId: ComponentId): Boolean = componentId in type

    /** Returns the archetype associated with adding [componentId] to this archetype's [type]. */
    operator fun plus(componentId: ComponentId): Archetype {
        return componentAddEdges.getOrElse(componentId.toLong()) {
            val archetype = archetypeProvider.getArchetype(type + componentId)
            updateComponentEdgesFor(componentId, archetype)
            archetype
        }
    }

    private fun updateComponentEdgesFor(
        componentId: ComponentId,
        archetype: Archetype,
    ) {
        componentAddEdges[componentId.toLong()] = archetype
        archetype.componentRemoveEdges[componentId.toLong()] = this
    }

    /** Returns the archetype associated with removing [componentId] to this archetype's [type]. */
    operator fun minus(componentId: ComponentId): Archetype =
        componentRemoveEdges.getOrPut(componentId.toLong()) {
            archetypeProvider.getArchetype(type.minus(componentId))
                .also { it.componentAddEdges[componentId.toLong()] = this }
        }

    // ==== Entity mutation ====
    /** Moves an entity from [oldArc] to this archetype when setting a component on the entity */
    private fun moveWithNewComponent(
        oldArc: Archetype,
        oldRow: Int,
        newComponent: Component,
        newComponentId: ComponentId,
        entity: EntityId,
    ) = move(entity) {
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

    /** Moves an entity from [oldArc] to this archetype when adding a component on the entity */
    private fun moveOnlyAdding(
        oldArc: Archetype,
        oldRow: Int,
        entity: EntityId,
    ) = move(entity) {
        for (i in 0..componentData.lastIndex) {
            componentData[i].add(oldArc.componentData[i][oldRow])
        }
    }

    /** Moves an entity from [oldArc] to this archetype when removing a component on the entity */
    private fun moveWithoutComponent(
        oldArc: Archetype,
        oldRow: Int,
        withoutComponentId: ComponentId,
        entity: EntityId,
    ) = move(entity) {
        val withoutCompIndex = oldArc.indexOf(withoutComponentId)

        // If removing a component that's added and not set, we just copy all data
        if (withoutCompIndex < 0) {
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


    internal fun createWithoutData(entity: EntityId): Int {
        ids.add(entity.toLong())
        return ids.lastIndex
    }

    internal inline fun move(
        entity: EntityId,
        copyData: () -> Unit,
    ): Int {
        if (unregistered) error("Tried adding entity to archetype that is no longer registered. Was it referenced outside of Geary?")
        ids.add(entity.toLong())
        val row = ids.lastIndex

        copyData()

        records[entity, this] = row
        return row
    }

    // For the following few functions, both entity and row are passed to avoid doing several array look-ups
    //  (ex when set calls remove).

    /**
     * Add a [componentId] to an entity represented by [record], moving it to the appropriate archetype.
     *
     * @return New archetype for entity
     */
    internal inline fun addComponent(
        row: Int,
        componentId: ComponentId,
        onUpdated: (Archetype, row: Int) -> Unit = { _, _ -> },
    ) {
        // if already present in this archetype, stop here since we don't need to update any data
        if (contains(componentId)) return

        val moveTo = this + (componentId.withoutRole(HOLDS_DATA))

        val entityId = ids[row].toULong()
        val newRow = moveTo.moveOnlyAdding(this, row, entityId)
        removeEntity(row)

        onUpdated(moveTo, newRow)
    }

    /**
     * Sets [data] at a [componentId] for an entity, moving it to the appropriate archetype.
     * Will ensure this component without [HOLDS_DATA] is always present.
     *
     * @return Whether the record has changed.
     */
    internal inline fun setComponent(
        row: Int,
        componentId: ComponentId,
        data: Component,
        onUpdated: (firstSet: Boolean, Archetype, row: Int) -> Unit = { _, _, _ -> },
    ) {
        val dataComponent = componentId.withRole(HOLDS_DATA)

        //If component already in this type, just update the data
        val addIndex = indexOf(dataComponent)
        if (addIndex >= 0) {
            componentData[addIndex][row] = data
            onUpdated(false, this, row)
            return
        }

        //if component is not already added, add it, then set
        val entityId = ids[row].toULong()
        val moveTo = this + dataComponent.withoutRole(HOLDS_DATA) + dataComponent
        val newRow = moveTo.moveWithNewComponent(this, row, data, dataComponent, entityId)
        removeEntity(row)

        // Component add listeners must query the target, this is an optimization
        onUpdated(true, moveTo, newRow)
    }

    /**
     * Removes a [component] from an entity at [row], moving it to the appropriate archetype.
     *
     * @return Whether the record has changed.
     */
    internal fun removeComponent(
        row: Int,
        component: ComponentId,
        onModify: (Archetype, row: Int, onComplete: (Archetype, Int) -> Unit) -> Unit = { a, r, onComplete ->
            onComplete(a, r)
        },
    ): Boolean {
        val entityId = ids[row].toULong()

        if (component !in type) return false

        // We run onModify before the component is removed, since observers will be interested in the component data,
        // Then take the result archetype after onModify and ensure the component is removed from there
        onModify(this, row) { finalArch, finalRow ->
            val moveTo = finalArch - component
            moveTo.moveWithoutComponent(finalArch, finalRow, component, entityId)
            finalArch.removeEntity(finalRow)
        }
        return true
    }

// TODO reimplement
//    private fun unregisterIfEmpty() {
//        if (allowUnregister == FALSE) return
//        if (ids.size == 0 && type.size != 0 && componentAddEdges.size == 0) {
//            if (allowUnregister == UNKNOWN) allowUnregister =
//                if (type.contains(comps.keepEmptyArchetype)) FALSE else TRUE
//            if (allowUnregister == FALSE) return
//            queryManager.unregisterArchetype(this)
//            unregistered = true
//            componentRemoveEdges.forEach { id, archetype ->
//                archetype.componentAddEdges.remove(id.toLong())
//                archetype.unregisterIfEmpty()
//            }
//            componentRemoveEdges.clear()
//        }
//    }

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
     * If a parameter is the [Any] component, the [HOLDS_DATA] role indicates whether other components
     * matched must also hold data themselves.
     * All other roles are ignored for the [target].
     */
    internal fun getRelations(kind: ComponentId, target: EntityId): List<Relation> {
        val specificKind = kind and ENTITY_MASK != ReservedComponents.ANY
        val specificTarget = target and ENTITY_MASK != ReservedComponents.ANY
        return when {
            specificKind && specificTarget -> listOf(Relation.of(kind, target))
            specificTarget -> getRelationsByTarget(target).map { Relation.of(it) }
            specificKind -> getRelationsByKind(kind).map { Relation.of(it) }
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
        relations: List<Relation>,
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
            componentData.fastForEach { it[row] = it.last() }
            records[replacement.toULong(), this@Archetype] = row
        }


        // Delete last row's data
        // If we're the last archetype in the chain with no entities, unregister to free up memory

        val index = ids.lastIndex
        if (index != -1) ids.removeAt(index)

        componentData.fastForEach { it.removeAt(lastIndex) }
//        unregisterIfEmpty() TODO reimplement
    }

    override fun equals(other: Any?): Boolean {
        return type == (other as? Archetype)?.type
    }

    companion object {
        private const val FALSE = 1.toByte()
        private const val UNKNOWN = 0.toByte()
        private const val TRUE = 2.toByte()
    }
}
