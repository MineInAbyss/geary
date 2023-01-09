package com.mineinabyss.geary.datatypes

import net.onedaybeard.bitvector.BitVector

actual class BitSet {
    @PublishedApi
    internal val inner: BitVector

    actual constructor() {
        inner = BitVector()
    }

    constructor(from: BitVector) {
        inner = from
    }

    actual operator fun get(index: Int): Boolean = inner[index]

    actual fun set(index: Int) {
        inner.set(index)
    }

    actual fun set(from: Int, to: Int) {
        for (i in from..to) inner.set(i)
    }

    actual fun clear(index: Int) {
        inner.clear(index)
    }

    actual fun flip(index: Int) {
        inner.flip(index)
    }

    actual fun and(other: BitSet) {
        inner.and(other.inner)
    }

    actual fun andNot(other: BitSet) {
        inner.andNot(other.inner)
    }

    actual fun or(other: BitSet) {
        inner.or(other.inner)
    }

    actual fun xor(other: BitSet) {
        inner.xor(other.inner)
    }

    actual inline fun forEachBit(crossinline loop: (Int) -> Unit) {
        inner.forEachBit(loop)
    }

    actual fun copy(): BitSet = BitSet(inner.copy())
    actual fun isEmpty(): Boolean = inner.isEmpty

    actual fun clear() {
        inner.clear()
    }

    actual val cardinality: Int get() = inner.cardinality()
}
