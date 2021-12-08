package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId

public open class ComponentAccessor<T : GearyComponent?>(
    index: Int,
    private val componentId: GearyComponentId,
) : Accessor<T>(index) {
    private val ArchetypeCacheScope.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> =
        listOf(archetype.componentData[dataIndex][row] as T)
}

public class ComponentOrDefaultAccessor<T : GearyComponent?>(
    index: Int,
    componentId: GearyComponentId,
    private val default: T
) : ComponentAccessor<T>(index, componentId) {
    private val ArchetypeCacheScope.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> {
        if(dataIndex == -1) return listOf(default)
        return listOf(archetype.componentData[dataIndex][row] as T)
    }
}
