package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.helpers.getComponentInfo
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * An inlined class used for tracking the components an entity/archetype has.
 *
 * It provides fast (no boxing) functions backed by FastUtil sorted sets to do operations with [GearyComponentId]s.
 */
@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
public value class GearyType private constructor(
    @PublishedApi
    internal val inner: ULongArray
) {
    public val size: Int get() = inner.size

    public constructor() : this(ULongArray(0))

    public constructor(ids: Collection<GearyComponentId>) :
            this(inner = ids.toULongArray().apply { sort() })
//            this(LongRBTreeSet().apply { for(id in ids) { add(id.toLong()) } })

    public operator fun contains(id: GearyComponentId): Boolean = inner.contains(id)

    public fun indexOf(id: GearyComponentId): Int {
        return binarySearch(id).coerceAtLeast(-1)
    }

    public tailrec fun binarySearch(id: GearyComponentId, fromIndex: Int = 0, toIndex: Int = inner.lastIndex): Int {
        if(fromIndex > toIndex) return -fromIndex - 1
        val mid = (fromIndex + toIndex) / 2
        val found = inner[mid]
        return when {
            found == id -> mid
            found < id -> binarySearch(id, mid + 1, toIndex)
            else -> binarySearch(id, fromIndex, mid - 1)
        }
    }

    public fun first(): GearyComponentId = inner.first()
    public fun last(): GearyComponentId = inner.last()

    public inline fun forEach(run: (GearyComponentId) -> Unit) {
        inner.forEach(run)
//        val iterator = inner.iterator()
//        while (iterator.hasNext()) {
//            run(iterator.nextLong().toULong())
//        }
    }

    public inline fun any(predicate: (GearyComponentId) -> Boolean): Boolean {
        forEach { if (predicate(it)) return true }
        return false
    }

    public inline fun forEachIndexed(run: (Int, GearyComponentId) -> Unit) {
        inner.forEachIndexed(run)
//        val iterator = inner.iterator()
//        var i = 0
//        forEach { run(i++, iterator.nextLong().toULong()) }
    }

    public inline fun filter(predicate: (GearyComponentId) -> Boolean): GearyType {
        return GearyType(inner.filter(predicate))
//        val type = LongAVLTreeSet()
//        forEach { if (predicate(it)) type.add(it.toLong()) }
//        return GearyType(type)
    }

    public inline fun <T> map(transform: (ULong) -> T): List<T> {
        return inner.map(transform)
    }

    public operator fun plus(id: GearyComponentId): GearyType {
        val search = binarySearch(id)
        if (search >= 0) return this
        val insertAt = -(search + 1)
        val arr = ULongArray(inner.size + 1)
        for (i in 0 until insertAt) arr[i] = inner[i]
        arr[insertAt] = id
        for (i in insertAt..inner.lastIndex) arr[i + 1] = inner[i]
        return GearyType(arr)
    }

    public operator fun minus(id: GearyComponentId): GearyType {
        val removeAt = binarySearch(id)
        if (removeAt < 0) return this
        val arr = ULongArray(inner.size - 1)
        for (i in 0 until removeAt) arr[i] = inner[i]
        for (i in (removeAt + 1)..inner.lastIndex) arr[i - 1] = inner[i]
        return GearyType(arr)

    }

    override fun toString(): String =
        inner.joinToString(", ", prefix = "[", postfix = "]") { it.readableString() }
}

public fun GearyEntityId.readableString(): String = buildString {
    val id = this@readableString
    if(id.hasRole(RELATION)) {
        append(id.toRelation().toString())
        return@buildString
    }
    if(id.hasRole(RELATION)) append("R") else append('-')
    if(id.hasRole(HOLDS_DATA)) append("D") else append('-')
    append(" ")
    val componentName = (id.getComponentInfo()?.kClass as? KClass<*>)?.simpleName
    if(componentName == null) append(id and ENTITY_MASK)
    else append(componentName)
}
