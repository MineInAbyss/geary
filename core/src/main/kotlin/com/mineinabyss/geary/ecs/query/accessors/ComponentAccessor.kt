package com.mineinabyss.geary.ecs.query.accessors

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.engine.iteration.*
import com.mineinabyss.geary.ecs.query.Query

public class ComponentAccessor<T : GearyComponent>(
    query: Query,
    private val componentId: GearyComponentId,
) : Accessor<T>(query) {
    private val ArchetypeIterator.dataIndex by cached { archetype.indexOf(componentId) }

    override fun AccessorData.readData(): List<T> =
        listOf(archetype.componentData[iterator.dataIndex][row] as T)
}
