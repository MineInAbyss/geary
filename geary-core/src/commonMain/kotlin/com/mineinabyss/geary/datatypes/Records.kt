package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.systems.accessors.AccessorThisRef

/**
 * A collection of records used for queries involving multiple entities.
 *
 * Currently built for our event system but will support arbitrary entities once we improve the query system.
 */
class Records(
    val target: AccessorThisRef,
    val event: AccessorThisRef,
    val source: AccessorThisRef?,
) {
    fun getByIndex(index: Int): AccessorThisRef {
        return when (index) {
            0 -> target
            1 -> event
            2 -> source ?: error("Source is null")
            else -> error("Index out of bounds")
        }
    }
}
