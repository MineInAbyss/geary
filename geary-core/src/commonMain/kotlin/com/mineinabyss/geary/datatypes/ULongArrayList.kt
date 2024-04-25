package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.toGeary

private const val initialSize: Int = 4
private const val growFactor: Int = 2

class ULongArrayList {
    var backingArr = ULongArray(initialSize)
    var size = 0
    val lastIndex get() = size - 1

    operator fun get(index: Int): ULong = backingArr[index]
    operator fun set(index: Int, value: ULong) {
        backingArr[index] = value
    }

    fun add(value: ULong) {
        if (size == backingArr.size) {
            grow()
        }
        backingArr[size++] = value
    }

    fun grow(){
        backingArr = backingArr.copyOf(size * growFactor)
    }

    fun removeLastOrNull(): ULong? {
        if (size == 0) return null
        return backingArr[--size]
    }

    fun removeLast(): ULong {
        return backingArr[--size]
    }

    internal fun removeAt(index: Int) {
        if (index == -1) return
        // replace with last
        backingArr[index] = backingArr[--size]
    }

    fun getEntities(): Sequence<Entity> {
        return backingArr.asSequence().take(size).map { it.toGeary() }
    }

    fun indexOf(value: ULong): Int {
        var n = 0
        val size = size
        while (n < size)
            if (backingArr[n++] == value) return n - 1
        return -1
    }
}
