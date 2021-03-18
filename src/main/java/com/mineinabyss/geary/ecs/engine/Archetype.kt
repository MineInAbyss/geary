package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import java.util.*

public data class Archetype(
    public val type: GearyType,
) {
    /** Component ids in the type that are to hold data */
    private val dataHoldingType = type.filter { it and HOLDS_DATA != 0uL }

    private fun indexOf(id: GearyComponentId): Int = dataHoldingType.indexOf(id)

    public val size: Int get() = ids.size

    internal val ids: MutableList<GearyEntityId> = mutableListOf()

    //TODO Use a hashmap here and make sure errors still get thrown if component ids are ever wrong
    private val componentData: List<MutableList<GearyComponent>> = dataHoldingType.map { mutableListOf() }

    public operator fun get(row: Int, component: GearyComponentId): GearyComponent? {
        val compIndex = indexOf(component)
        if (compIndex == -1) return null

        return componentData[compIndex][row]
    }

    internal val add = mutableMapOf<GearyComponentId, Archetype>()
    internal val remove = mutableMapOf<GearyComponentId, Archetype>()

    public operator fun plus(id: GearyComponentId): Archetype {
        return add[id] ?: type.plus(id).getArchetype()
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
        // if component should hold data, stop here
        if (component and HOLDS_DATA != 0uL) return null
        // if already present in this archetype, stop here since we dont need to update any data
        if (component in type) return null

        val moveTo = this + component

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
        // if component should NOT hold data, stop here
        if (component and HOLDS_DATA == 0uL) return null

        //if component was added but not set, remove the old component before adding this one
        val addId = component and HOLDS_DATA.inv()
        if (addId in type) {
            val removedRecord = removeComponent(entity, record, addId)!!
            return removedRecord.archetype.setComponent(entity, removedRecord, component, data)
        }

        //If component already in this type, just update the data
        val addIndex = indexOf(component)
        if (addIndex != -1) {
            componentData[addIndex][record.row] = data
            return null
        }

        val moveTo = this + component
        val newCompIndex = moveTo.dataHoldingType.indexOf(component)
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

    //TODO stuff should only be added here if we are currently iterating
    /** Map of elements moved during a component removal. Represents the resulting row to original row. */
    private val movedRows = mutableSetOf<Int>()

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
            //TODO I'd like this to perhaps be independent of engine in case we ever want more than one at a time
            Engine.setRecord(replacement, Record(this, row))
            movedRows.add(row)
        }
    }

    internal class ArchetypeIterator(
        private val archetype: Archetype,
        private val type: GearyType
    ) : Iterator<Pair<GearyEntity, List<GearyComponent>>> {
        private val typeDataIndices = type
            .filter { it and HOLDS_DATA != 0uL }
            .map { archetype.indexOf(it) }
        private var row = 0
        override fun hasNext(): Boolean {
            return (row < archetype.size || archetype.movedRows.isNotEmpty())
                .also { if (!it) archetype.movedRows.clear() }
        }

        override fun next(): Pair<GearyEntity, List<GearyComponent>> {
            //TODO is there a more efficient way of taking any element from the set and removing it?
            val destinationRow = archetype.movedRows.firstOrNull() ?: return getAtIndex(row++)
            archetype.movedRows.remove(destinationRow)
            return getAtIndex(destinationRow)
        }

        private fun getAtIndex(index: Int): Pair<GearyEntity, List<GearyComponent>> {
            return geary(archetype.ids[index]) to typeDataIndices.map {
                archetype.componentData[it][index]
            }
        }
    }
}
