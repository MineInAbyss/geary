package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.engine.iteration.accessors.ArchetypeCache
import com.mineinabyss.geary.ecs.engine.iteration.accessors.RawAccessorDataScope

public class ComponentAccessor<T : GearyComponent>(
    index: Int,
    private val componentId: GearyComponentId,
) : Accessor<T>(index) {
    private val ArchetypeCache.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> =
        listOf(archetype.componentData[dataIndex][row] as T)
}
