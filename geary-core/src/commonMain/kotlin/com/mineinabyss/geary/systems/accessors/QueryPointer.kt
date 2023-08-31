package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.Record
import kotlin.jvm.JvmInline

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
