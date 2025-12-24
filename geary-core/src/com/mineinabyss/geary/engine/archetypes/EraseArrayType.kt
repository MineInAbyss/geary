package com.mineinabyss.geary.engine.archetypes

import kotlin.jvm.JvmField

class EraseArrayType<T>(@JvmField val array: Array<T>) {
    inline operator fun get(index: Int): T = array[index]
    inline operator fun set(index: Int, value: T) { array[index] = value }

    context(row: Int)
    operator fun component1(): T = array[row]
}