package com.mineinabyss.geary.datatypes

/**
 * Cross-platform interface for a bitset.
 */
expect class BitSet() {
    //    public val length: Int
    fun isEmpty(): Boolean
    operator fun get(index: Int): Boolean
    fun set(index: Int)
    fun set(from: Int, to: Int)
    fun clear(index: Int)
    fun flip(index: Int)
    fun and(other: BitSet)
    fun andNot(other: BitSet)
    fun or(other: BitSet)
    fun xor(other: BitSet)
    fun clear()
    val cardinality: Int

    inline fun forEachBit(crossinline loop: (Int) -> Unit)

    fun copy(): BitSet

}

fun bitsOf(): BitSet = BitSet()
