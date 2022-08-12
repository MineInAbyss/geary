package com.mineinabyss.geary.systems.accessors.types

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.systems.accessors.ArchetypeCacheScope
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope

/**
 * Implements the accessor [get] operation.
 *
 * @see get
 */
//TODO is it possible to merge with ComponentOrDefaultAccessor
public open class ComponentAccessor<T : Component?>(
    index: Int,
    private val componentId: ComponentId,
) : IndexedAccessor<T>(index) {
    private val ArchetypeCacheScope.dataIndex by cached { archetype.indexOf(componentId) }

    override fun RawAccessorDataScope.readData(): List<T> =
        @Suppress("UNCHECKED_CAST") // Index assignment ensures this should always be true
        listOf(archetype.componentData[dataIndex][row] as T)
}

