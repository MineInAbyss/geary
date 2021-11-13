package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.engine.iteration.*

public class ComponentAccessor<T : GearyComponent>(
    index: Int,
    private val componentId: GearyComponentId,
) : Accessor<T>(index) {
    private val ArchetypeIterator.dataIndex by cached { archetype.indexOf(componentId) }

    override fun AccessorDataScope.readData(): List<T> =
        listOf(archetype.componentData[iterator.dataIndex][row] as T)
}
