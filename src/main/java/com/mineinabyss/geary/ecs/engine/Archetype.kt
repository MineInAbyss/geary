package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyEntityId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.engine.types.HOLDS_DATA

public class Archetype(
    public val type: GearyType,
) {
    /** Component ids in the type that are to hold data */
    private val dataHoldingType = type.filter { it and HOLDS_DATA != 0uL }

    //TODO find internal index more efficiently, currently O(N)
    private fun indexOf(id: GearyComponentId): Int = type.indexOf(id)

    public val size: Int get() = ids.size

    private val ids: MutableList<GearyEntityId> = mutableListOf()

    private val componentData: List<MutableList<GearyComponent>> = dataHoldingType.map { mutableListOf() }

    public operator fun get(row: Int, component: GearyComponentId): GearyComponent? {
        val compIndex = indexOf(component)
        if (compIndex == -1) return null

        return componentData[compIndex][row]
    }


    //edges
    //TODO can we use a smarter structure than map here?
    internal val add = mutableMapOf<GearyComponentId, Archetype>()
    internal val remove = mutableMapOf<GearyComponentId, Archetype>()

    public operator fun plus(id: GearyComponentId): Archetype {
        return add[id] ?: type.plusSorted(id).getArchetype()
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
        if (component and HOLDS_DATA != 0uL) return null

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

        removeEntity(record.row)
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
        this.componentData.forEachIndexed { i, it ->
            if (type[i] != component)
                componentData.add(it[record.row])
        }

        removeEntity(record.row)
        return moveTo.addEntityWithData(entity, componentData)
    }

    internal fun getComponents(row: Int): ArrayList<GearyComponent> =
        componentData.mapTo(arrayListOf()) { it[row] }

    @Synchronized
    internal fun removeEntity(row: Int) {
        ids[row] = ids.last()
        componentData.forEach { it[row] = componentData.last() }
        ids.removeLastOrNull()
        componentData.forEach { it.removeLastOrNull() }
        //TODO move iterators back one index so they dont just skip the entity we replaced
    }
}
