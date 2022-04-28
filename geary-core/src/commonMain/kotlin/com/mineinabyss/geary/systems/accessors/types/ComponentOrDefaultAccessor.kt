package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.systems.accessors.ArchetypeCacheScope
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope

/**
 * Implements the accessor [getOrDefault] operation.
 *
 * @see getOrDefault
 */
public class ComponentOrDefaultAccessor<T : GearyComponent?>(
    index: Int,
    componentId: GearyComponentId,
    private val default: T
) : ComponentAccessor<T>(index, componentId) {
    private val ArchetypeCacheScope.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> {
        if (dataIndex == -1) return listOf(default)
        @Suppress("UNCHECKED_CAST") // Index assignment ensures this should always be true
        return listOf(archetype.componentData[dataIndex][row] as T)
    }
}
