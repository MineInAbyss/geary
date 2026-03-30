package com.mineinabyss.geary.query

import androidx.collection.MutableIntList
import com.mineinabyss.geary.datatypes.SparseSet

class Query(
    val matchedSets: Array<SparseSet<*>>,
) {
    val indices = MutableIntList()
    inline fun forEach(run: (Int) -> Unit) {
        indices.clear()
        matchedSets.sortBy { it.size }
        matchedSets.first().forEachIndexed { entityId, data ->
            for (setId in 1..matchedSets.lastIndex) {
                if (entityId !in matchedSets[setId]) return@forEachIndexed
            }
            indices.add(entityId)
        }
        indices.forEach(run)
    }
}