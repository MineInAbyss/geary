package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyComponentId
import com.mineinabyss.geary.systems.accessors.ArchetypeCacheScope
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope

/**
 * Implements the accessor [get] operation.
 *
 * @see get
 */
//TODO is it possible to merge with ComponentOrDefaultAccessor
public open class ComponentAccessor<T : GearyComponent?>(
    index: Int,
    private val componentId: GearyComponentId,
) : IndexedAccessor<T>(index) {
    private val ArchetypeCacheScope.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> =
        @Suppress("UNCHECKED_CAST") // Index assignment ensures this should always be true
        listOf(archetype.componentData[dataIndex][row] as T)
}

