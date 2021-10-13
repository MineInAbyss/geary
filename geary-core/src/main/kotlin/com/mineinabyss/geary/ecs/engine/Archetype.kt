package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import com.mineinabyss.geary.ecs.api.relations.toRelation
import com.mineinabyss.geary.ecs.api.systems.ComponentAddSystem
import com.mineinabyss.geary.ecs.engine.iteration.ArchetypeIterator
import com.mineinabyss.geary.ecs.query.AndSelector
import com.mineinabyss.geary.ecs.query.Query
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.util.*

public typealias Event = GearyEntity.() -> Unit

public data class Archetype(
    public val type: GearyType,
) {
    /** Component ids in the type that are to hold data */
    // Currently all relations must hold data and the HOLDS_DATA bit on them corresponds to the component part.
    private val dataHoldingType = type.filter { it.holdsData() || it.isRelation() }

    /** Map of relation parent id to a list of relations with that parent */
    //TODO List<Relation>
    internal val relations: Long2ObjectOpenHashMap<MutableList<Relation>> = type
        .mapNotNull { it.toRelation() }
        .groupByTo(Long2ObjectOpenHashMap()) { it.parent.id.toLong() }

    internal val dataHoldingRelations: Long2ObjectOpenHashMap<List<Relation>> by lazy {
        val map = Long2ObjectOpenHashMap<List<Relation>>()
        relations.forEach { (key, value) ->
            val dataHolding = value.filter { it.component.holdsData() }
            if (dataHolding.isNotEmpty()) map[key] = dataHolding
        }
        map
    }

    /** @return This Archetype's [relations] that are also a part of [matchRelations]. */
    public fun matchedRelationsFor(matchRelations: Collection<RelationParent>): Map<RelationParent, List<Relation>> =
        matchRelations
            .filter { it.id.toLong() in relations }
            .associateWith { relations[it.id.toLong()]!! } //TODO handle null error

    private val comp2indices = Long2IntOpenHashMap().apply {
        dataHoldingType.forEachIndexed { i, compId -> put(compId.toLong(), i) }
        defaultReturnValue(-1)
    }

    internal fun indexOf(id: GearyComponentId): Int = comp2indices.get(id.toLong())

    public val size: Int get() = ids.size

    internal val ids: MutableList<GearyEntityId> = mutableListOf()

    //TODO Use a hashmap here and make sure errors still get thrown if component ids are ever wrong
    internal val componentData: List<MutableList<GearyComponent>> = dataHoldingType.map { mutableListOf() }

    public operator fun get(row: Int, component: GearyComponentId): GearyComponent? {
        val compIndex = indexOf(component)
        if (compIndex == -1) return null

        return componentData[compIndex][row]
    }

    public operator fun contains(component: GearyComponentId): Boolean =
        // Check if contains component or the version with the HOLDS_DATA bit flipped
        component in type || component.withInvertedRole(HOLDS_DATA) in type

    internal val add = mutableMapOf<GearyComponentId, Archetype>()
    internal val remove = mutableMapOf<GearyComponentId, Archetype>()

    public operator fun plus(id: GearyComponentId): Archetype {
        return add[id] ?: type.let {
            // Ensure that when adding an ID that holds data, we remove the non-data-holding ID
            if (id.holdsData() && !id.isRelation())
                it.minus(id.withoutRole(HOLDS_DATA))
            else it
        }.plus(id).getArchetype()
    }

    public operator fun minus(id: GearyComponentId): Archetype {
        return remove[id] ?: type.minus(id).getArchetype().also {
            remove[id] = it
        }
    }

    /**
     * @param data A list of components whose indices correctly match those of this archetype's [dataHoldingType]
     */
    @Synchronized
    internal fun addEntityWithData(
        entity: GearyEntityId,
        data: List<GearyComponent>
    ): Record {
        ids.add(entity)
        componentData.forEachIndexed { i, compArray ->
            compArray.add(data[i])
        }
        return Record(this, size - 1)
    }

    @Synchronized
    internal fun addComponent(
        entity: GearyEntityId,
        record: Record,
        component: GearyComponentId
    ): Record? {
        // if already present in this archetype, stop here since we dont need to update any data
        if (contains(component)) return null

        val moveTo = this + (component.withoutRole(HOLDS_DATA))

        val componentData = getComponents(record.row)
        return moveTo.addEntityWithData(entity, componentData).also { removeEntity(record.row) }
    }

    @Synchronized
    internal fun setComponent(
        entity: GearyEntityId,
        record: Record,
        component: GearyComponentId,
        data: GearyComponent
    ): Record? {
        val isRelation = component.isRelation()

        // Relations should not add the HOLDS_DATA bit since the type roles are of the relation's child
        val dataComponent = if (isRelation) component else component.withRole(HOLDS_DATA)

        //if component was added but not set, remove the old component before adding this one
        val addId = dataComponent.withoutRole(HOLDS_DATA)
        if (addId in type) {
            val removedRecord = removeComponent(entity, record, addId)!!
            return removedRecord.archetype.setComponent(entity, removedRecord, dataComponent, data)
        }

        //If component already in this type, just update the data
        val addIndex = indexOf(dataComponent)
        if (addIndex != -1) {
            componentData[addIndex][record.row] = data
            return null
        }

        val moveTo = this + dataComponent
        val newCompIndex = moveTo.dataHoldingType.indexOf(dataComponent)
        val componentData = getComponents(record.row).apply {
            add(newCompIndex, data)
        }

        return moveTo.addEntityWithData(entity, componentData).also { removeEntity(record.row) }
    }

    @Synchronized
    internal fun removeComponent(
        entity: GearyEntityId,
        record: Record,
        component: GearyComponentId
    ): Record? {
        if (component !in type) return null

        val moveTo = this - component

        val componentData = mutableListOf<GearyComponent>()

        val skipData = indexOf(component)
        this.componentData.forEachIndexed { i, it ->
            if (i != skipData)
                componentData.add(it[record.row])
        }

        removeEntity(record.row)
        return moveTo.addEntityWithData(entity, componentData)
    }

    internal fun getComponents(row: Int): ArrayList<GearyComponent> =
        componentData.mapTo(arrayListOf()) { it[row] }

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
            runningIterators.keys.forEach {
                it.addMovedRow(lastIndex, row)
            }
            Engine.setRecord(replacement, Record(this, row))
        }
    }

    // Some systems that get called when components get modified

    public fun addComponentAddSystem(system: ComponentAddSystem) {
        (system.family as AndSelector).components
            .map { indexOf(it) }
            .forEach { componentAddSystems[it] += system }
    }

    public fun runComponentAddSystems(forComponent: GearyComponentId, entity: GearyEntity) {
        val index = indexOf(forComponent)
        if (index == -1) return
        componentAddSystems[index].forEach { it(entity) }
    }

    private val componentAddSystems = Array(dataHoldingType.size) { mutableSetOf<ComponentAddSystem>() }

    // Basically just want a weak set where stuff gets auto removed when it is no longer running
    // We put our iterator and null and this WeakHashMap handles the rest for us.
    private val runningIterators = WeakHashMap<ArchetypeIterator, Any?>()
    private val queryIterators = mutableMapOf<Query, ArchetypeIterator>()

    internal fun finalizeIterator(iterator: ArchetypeIterator) {
        runningIterators.remove(iterator)
    }

    internal fun iteratorFor(query: Query): ArchetypeIterator {
        val iterator = queryIterators.getOrPut(query) { ArchetypeIterator(this, query) }.copy()
        runningIterators[iterator] = null
        return iterator
    }
}
