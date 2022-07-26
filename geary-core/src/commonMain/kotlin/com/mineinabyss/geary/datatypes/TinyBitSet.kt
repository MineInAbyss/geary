package com.mineinabyss.geary.datatypes

// Extra Long operations to be able to have a really fast tiny bitset

public fun Long.pop1(): Long {
    val c = countTrailingZeroBits()
    if (c == 64) return this
    return unsetBit(c)
}

public fun Long.setBit(index: Int): Long {
    return this or (1L shl index)
}

public fun Long.unsetBit(index: Int): Long {
    return this and (1L shl index).inv()
}

public inline fun Long.forEachBit(run: (Int) -> Unit) {
    var remaining = this
    while (remaining != 0L) {
        val hi = remaining.takeHighestOneBit()
        remaining = remaining and hi.inv()
        run(hi.countTrailingZeroBits())
    }
}

public fun Long.toIntArray(): IntArray {
    val arr = IntArray(countOneBits())
    var i = 0
    forEachBit { arr[i] = it; i++ }
    return arr
}
