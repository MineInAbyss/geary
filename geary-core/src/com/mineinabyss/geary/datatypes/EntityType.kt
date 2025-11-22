package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.readableString


/**
 * Immutable array of [ComponentId]s used to represent the 'type' of an [com.mineinabyss.geary.engine.archetypes.Archetype].
 *
 * It provides fast (no boxing) functions backed by FastUtil sorted sets to do operations with [ComponentId]s.
 */
class EntityType private constructor(
    @PublishedApi
    internal val inner: ULongArray,
) {
    constructor() : this(ULongArray(0))


    constructor(ids: Collection<ComponentId>) : this(inner = ids.toULongArray().apply { sort() })

    val size: Int = inner.size

    operator fun contains(id: ComponentId): Boolean = indexOf(id) != -1

    fun indexOf(id: ComponentId): Int {
        return inner.indexOf(id)
    }

    tailrec fun binarySearch(id: ComponentId, fromIndex: Int = 0, toIndex: Int = size - 1): Int {
        if (fromIndex > toIndex) return -fromIndex - 1
        val mid = (fromIndex + toIndex) / 2
        val found = inner[mid]
        return when {
            found == id -> mid
            found < id -> binarySearch(id, mid + 1, toIndex)
            else -> binarySearch(id, fromIndex, mid - 1)
        }
    }

    fun first(): ComponentId = inner.first()
    fun last(): ComponentId = inner.last()

    inline fun forEach(run: (ComponentId) -> Unit) {
        for (i in 0..inner.lastIndex) {
            run(inner[i])
        }
    }

    inline fun any(predicate: (ComponentId) -> Boolean): Boolean {
        forEach { if (predicate(it)) return true }
        return false
    }

    inline fun forEachIndexed(run: (Int, ComponentId) -> Unit) {
        inner.forEachIndexed(run)
    }

    inline fun filter(predicate: (ComponentId) -> Boolean): EntityType {
        return EntityType(inner.filter(predicate))
    }

    inline fun <T> map(transform: (ULong) -> T): List<T> {
        return inner.map(transform)
    }

    operator fun plus(id: ComponentId): EntityType {
        val search = binarySearch(id)
        if (search >= 0) return this
        val insertAt = -(search + 1)
        val arr = ULongArray(inner.size + 1)
        for (i in 0 until insertAt) arr[i] = inner[i]
        arr[insertAt] = id
        for (i in insertAt..inner.lastIndex) arr[i + 1] = inner[i]
        return EntityType(arr)
    }

    operator fun plus(other: EntityType): EntityType {
        return EntityType(inner.plus(other.inner))
    }

    operator fun minus(id: ComponentId): EntityType {
        val removeAt = binarySearch(id)
        if (removeAt < 0) return this
        val arr = ULongArray(inner.size - 1)
        for (i in 0 until removeAt) arr[i] = inner[i]
        for (i in (removeAt + 1)..inner.lastIndex) arr[i - 1] = inner[i]
        return EntityType(arr)
    }

    override fun toString(): String =
        inner.joinToString(", ", prefix = "[", postfix = "]") { it.readableString(null) }
}


fun entityTypeOf(vararg ids: ComponentId): EntityType {
    return EntityType(ids.toList())
}
