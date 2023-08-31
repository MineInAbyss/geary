package com.mineinabyss.geary.datatypes

//typealias IdList = ArrayList<ULong>
// TODO more efficient type, no boxing
class IdList(initialSize: Int = 16, val growFactor: Int = 2) : Iterable<ULong> {
    var backingArr = ULongArray(initialSize)
    var size = 0
    val lastIndex get() = size - 1

    operator fun get(index: Int): ULong = backingArr[index]
    operator fun set(index: Int, value: ULong) {
        backingArr[index] = value
    }

    fun add(value: ULong) {
        if (size == backingArr.size) {
            backingArr = backingArr.copyOf(size * growFactor)
        }
        backingArr[size++] = value
    }

    fun removeLastOrNull(): ULong? {
        if (size == 0) return null
        return backingArr[--size]
    }

    override fun iterator(): Iterator<ULong> {
        return backingArr.iterator()
    }
}
