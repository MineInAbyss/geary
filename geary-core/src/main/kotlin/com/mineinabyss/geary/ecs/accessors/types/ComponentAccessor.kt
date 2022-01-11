package com.mineinabyss.geary.ecs.accessors.types

import com.mineinabyss.geary.ecs.accessors.Accessor
import com.mineinabyss.geary.ecs.accessors.ArchetypeCacheScope
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId

/**
 * Implements the accessor [get] operation.
 *
 * @see get
 */
//TODO is it possible to merge with ComponentOrDefaultAccessor
public open class ComponentAccessor<T : GearyComponent?>(
    index: Int,
    private val componentId: GearyComponentId,
) : Accessor<T>(index) {
    private val ArchetypeCacheScope.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> =
        @Suppress("UNCHECKED_CAST") // Index assignment ensures this should always be true
        listOf(archetype.componentData[dataIndex][row] as T)
}

