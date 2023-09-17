package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.systems.accessors.Pointer

/**
 * A collection of records used for queries involving multiple entities.
 *
 * Currently built for our event system but will support arbitrary entities once we improve the query system.
 */
class Records(
    val target: Pointer,
    val event: Pointer,
    val source: Pointer?,
) {
    fun getByIndex(index: Int): Pointer {
        return when (index) {
            0 -> target
            1 -> event
            2 -> source ?: error("Source is null")
            else -> error("Index out of bounds")
        }
    }
}
