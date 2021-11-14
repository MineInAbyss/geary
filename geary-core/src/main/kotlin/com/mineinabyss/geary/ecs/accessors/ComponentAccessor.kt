package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId

public class ComponentAccessor<T : GearyComponent>(
    index: Int,
    private val componentId: GearyComponentId,
) : Accessor<T>(index) {
    private val ArchetypeCacheScope.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> =
        listOf(archetype.componentData[dataIndex][row] as T)
}
