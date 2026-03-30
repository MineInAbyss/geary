package com.mineinabyss.geary.query

import androidx.collection.MutableScatterMap
import com.mineinabyss.geary.datatypes.SparseSet

/**
 * Maps component ids to a [com.mineinabyss.geary.datatypes.SparseSet]
 */
class ComponentIndex {
    private val idToDataMap = MutableScatterMap<Long, SparseSet<*>>()

    operator fun <T> get(id: Long): SparseSet<T> {
        val existing = idToDataMap[id]
        if (existing != null) return existing as SparseSet<T>
        val new = SparseSet<T>()
        idToDataMap[id] = new
        return new
    }
}