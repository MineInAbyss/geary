package com.mineinabyss.geary.datatypes

import org.roaringbitmap.IntConsumer
import org.roaringbitmap.RoaringBitmap

public actual class BitSet {
    @PublishedApi
    internal val inner: RoaringBitmap

    public actual constructor() {
        inner = RoaringBitmap()
    }

    public constructor(from: RoaringBitmap) {
        inner = from
    }

    public actual fun isEmpty(): Boolean = inner.isEmpty

    public actual operator fun get(index: Int): Boolean =
        inner.contains(index)

    public actual fun set(index: Int) {
        inner.add(index)
    }

    public actual fun set(from: Int, to: Int) {
        inner.add(from, to)
    }

    public actual fun clear(index: Int) {
        inner.remove(index)
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

    public actual fun clear() {
        inner.clear()
    }

    public actual val cardinality: Int
        get() = inner.cardinality


    public actual inline fun forEachBit(crossinline loop: (Int) -> Unit) {
        // Roaring bitsets run into concurrent modification issues where clearing a bit might skip iterating another
        // so we have to clone the set.
        inner.clone().forEach(IntConsumer { loop(it) })
    }

    public actual fun copy(): BitSet = BitSet(inner.clone())
}
