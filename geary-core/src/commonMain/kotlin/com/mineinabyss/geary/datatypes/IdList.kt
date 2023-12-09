package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.toGeary

private const val initialSize: Int = 4
private const val growFactor: Int = 2

class IdList {
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

    fun getEntities(): Sequence<Entity> {
        return backingArr.asSequence().take(size).map { it.toGeary() }
    }
}
