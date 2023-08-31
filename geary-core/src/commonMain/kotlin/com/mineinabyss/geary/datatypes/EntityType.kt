package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.readableString
import kotlin.jvm.JvmInline

/**
 * An inlined class used for tracking the components an entity/archetype has.
 *
 * It provides fast (no boxing) functions backed by FastUtil sorted sets to do operations with [ComponentId]s.
 */
@JvmInline
value class EntityType private constructor(
    @PublishedApi
    internal val inner: ULongArray
) {
    constructor() : this(ULongArray(0))


    constructor(ids: Collection<ComponentId>) : this(inner = ids.toULongArray().apply { sort() })

    val size: Int get() = inner.size

    val prefabs: EntityType
        get() = EntityType(filter { contains(Relation.of(componentId<InstanceOf>(), it).id) }
            .map { Relation.of(it).target })

    operator fun contains(id: ComponentId): Boolean = indexOf(id) != -1

    fun indexOf(id: ComponentId): Int {
        return binarySearch(id).coerceAtLeast(-1)
    }

    tailrec fun binarySearch(id: ComponentId, fromIndex: Int = 0, toIndex: Int = inner.lastIndex): Int {
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
        for(i in 0..inner.lastIndex) {
            run(inner[i])
        }
//        val iterator = inner.iterator()
//        while (iterator.hasNext()) {
//            run(iterator.nextLong().toULong())
//        }
    }

    inline fun any(predicate: (ComponentId) -> Boolean): Boolean {
        forEach { if (predicate(it)) return true }
        return false
    }

    inline fun forEachIndexed(run: (Int, ComponentId) -> Unit) {
        inner.forEachIndexed(run)
//        val iterator = inner.iterator()
//        var i = 0
//        forEach { run(i++, iterator.nextLong().toULong()) }
    }

    inline fun filter(predicate: (ComponentId) -> Boolean): EntityType {
        return EntityType(inner.filter(predicate))
//        val type = LongAVLTreeSet()
//        forEach { if (predicate(it)) type.add(it.toLong()) }
//        return GearyType(type)
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

    //TODO intersection and union

    override fun toString(): String =
        inner.joinToString(", ", prefix = "[", postfix = "]") { it.readableString() }

    companion object {
        internal fun fromMutableArray(array: ULongArray): EntityType {
            array.sort()
            return EntityType(array)
        }
    }
}
