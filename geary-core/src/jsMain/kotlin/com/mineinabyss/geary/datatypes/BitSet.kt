package com.mineinabyss.geary.datatypes

actual class BitSet actual constructor() {
    actual fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    actual operator fun get(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun set(index: Int) {
    }

    actual fun set(from: Int, to: Int) {
    }

    actual fun clear(index: Int) {
    }

    actual fun clear() {
    }

    actual fun flip(index: Int) {
    }

    actual fun and(other: BitSet) {
    }

    actual fun andNot(other: BitSet) {
    }

    actual fun or(other: BitSet) {
    }

    actual fun xor(other: BitSet) {
    }

    actual val cardinality: Int
        get() = TODO("Not yet implemented")

    actual inline fun forEachBit(crossinline loop: (Int) -> Unit) {
    }

    actual fun copy(): BitSet {
        TODO("Not yet implemented")
    }
}
