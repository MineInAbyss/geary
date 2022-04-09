package com.mineinabyss.geary.ecs.api.systems

public expect class BitSet() {
//    public val length: Int
    public operator fun get(index: Int): Boolean
    public fun set(index: Int)
    public fun clear(index: Int)
    public fun flip(index: Int)
    public fun and(other: BitSet)
    public fun andNot(other: BitSet)
    public fun or(other: BitSet)
    public fun xor(other: BitSet)

    public inline fun forEachBit(crossinline loop: (Int) -> Unit)

    public fun copy(): BitSet
}

public fun bitsOf(): BitSet = BitSet()
//public fun BitSet.set(index: Int, boolean: Boolean) {
//
//}
