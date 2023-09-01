package com.mineinabyss.geary.datatypes

/**
 * A collection of records used for queries involving multiple entities.
 *
 * Currently built for our event system but will support arbitrary entities once we improve the query system.
 */
class Records(
    val target: Record,
    val event: Record,
    val source: Record?,
) {
    fun getByIndex(index: Int): Record {
        return when (index) {
            0 -> target
            1 -> event
            2 -> source ?: error("Source is null")
            else -> error("Index out of bounds")
        }
    }
}
