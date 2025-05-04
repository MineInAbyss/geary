/**
 * Taken from korlibs KDS, licensed under MIT https://github.com/korlibs/korge/tree/main/korlibs-datastructure
 */
package com.mineinabyss.geary.helpers

inline fun <T> List<T>.fastForEach(callback: (T) -> Unit) {
    var n = 0
    while (n < size) callback(this[n++])
}

inline fun <T> Array<T>.fastForEach(callback: (T) -> Unit) {
    var n = 0
    while (n < size) callback(this[n++])
}

inline fun IntArray.fastForEach(callback: (Int) -> Unit) {
    var n = 0
    while (n < size) callback(this[n++])
}

inline fun FloatArray.fastForEach(callback: (Float) -> Unit) {
    var n = 0
    while (n < size) callback(this[n++])
}

inline fun DoubleArray.fastForEach(callback: (Double) -> Unit) {
    var n = 0
    while (n < size) callback(this[n++])
}

inline fun <T> List<T>.fastForEachWithIndex(callback: (index: Int, value: T) -> Unit) {
    var n = 0
    while (n < size) {
        callback(n, this[n])
        n++
    }
}

inline fun <T> Array<T>.fastForEachWithIndex(callback: (index: Int, value: T) -> Unit) {
    var n = 0
    while (n < size) {
        callback(n, this[n])
        n++
    }
}

inline fun <T> List<T>.fastForEachReverse(callback: (T) -> Unit) {
    var n = 0
    while (n < size) {
        callback(this[size - n - 1])
        n++
    }
}

inline fun <T> MutableList<T>.fastIterateRemove(callback: (T) -> Boolean): MutableList<T> {
    var n = 0
    var m = 0
    while (n < size) {
        if (m >= 0 && m != n) this[m] = this[n]
        if (callback(this[n])) m--
        n++
        m++
    }
    while (this.size > m) this.removeAt(this.size - 1)
    return this
}
