package com.mineinabyss.geary.ecs.api.systems

import net.onedaybeard.bitvector.BitVector

public actual class BitSet {
    @PublishedApi
    internal val inner: BitVector

    public actual constructor() {
        inner = BitVector()
    }

    public constructor(from: BitVector) {
        inner = from
    }

    public actual operator fun get(index: Int): Boolean = inner[index]

    public actual fun set(index: Int) {
        inner.set(index)
    }

    public actual fun clear(index: Int) {
        inner.clear(index)
    }

    public actual fun flip(index: Int) {
        inner.flip(index)
    }

    public actual fun and(other: BitSet) {
        inner.and(other.inner)
    }

    public actual fun andNot(other: BitSet) {
        inner.andNot(other.inner)
    }

    public actual fun or(other: BitSet) {
        inner.or(other.inner)
    }

    public actual fun xor(other: BitSet) {
        inner.xor(other.inner)
    }

    public actual inline fun forEachBit(crossinline loop: (Int) -> Unit) {
        inner.forEachBit(loop)
    }

    public actual fun copy(): BitSet = BitSet(inner.copy())
}
