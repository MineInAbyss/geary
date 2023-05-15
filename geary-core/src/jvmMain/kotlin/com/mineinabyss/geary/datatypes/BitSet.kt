package com.mineinabyss.geary.datatypes

import org.roaringbitmap.IntConsumer
import org.roaringbitmap.RoaringBitmap

actual class BitSet {
    @PublishedApi
    internal val inner: RoaringBitmap

    actual constructor() {
        inner = RoaringBitmap()
    }

    constructor(from: RoaringBitmap) {
        inner = from
    }

    actual fun isEmpty(): Boolean = inner.isEmpty

    actual operator fun get(index: Int): Boolean =
        inner.contains(index)

    actual fun set(index: Int) {
        inner.add(index)
    }

    actual fun set(from: Int, to: Int) {
        inner.add(from, to)
    }

    actual fun clear(index: Int) {
        inner.remove(index)
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

    actual fun clear() {
        inner.clear()
    }

    actual val cardinality: Int
        get() = inner.cardinality


    actual inline fun forEachBit(crossinline loop: (Int) -> Unit) {
        // Roaring bitsets run into concurrent modification issues where clearing a bit might skip iterating another,
        // so we have to clone the set.
        inner.clone().forEach(IntConsumer { loop(it) })
    }

    actual fun copy(): BitSet = BitSet(inner.clone())
}
