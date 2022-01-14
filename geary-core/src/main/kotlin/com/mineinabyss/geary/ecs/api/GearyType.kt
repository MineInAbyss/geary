package com.mineinabyss.geary.ecs.api

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet
import it.unimi.dsi.fastutil.longs.LongSortedSet
import it.unimi.dsi.fastutil.longs.LongSortedSets

/**
 * An inlined class used for tracking the components an entity/archetype has.
 *
 * It provides fast (no boxing) functions backed by FastUtil sorted sets to do operations with [GearyComponentId]s.
 */
@JvmInline
public value class GearyType private constructor(
    public val inner: LongSortedSets.UnmodifiableSortedSet
) {
    public val size: Int get() = inner.size

    public constructor(vararg ids: GearyComponentId) : this(LongAVLTreeSet(ids.map { it.toLong() }))
    public constructor(ids: LongSortedSet) :
            this(LongSortedSets.unmodifiable(ids) as LongSortedSets.UnmodifiableSortedSet)

    public constructor(ids: Collection<GearyComponentId>) :
            this(LongAVLTreeSet(ids.map { it.toLong() }))

    public operator fun contains(id: GearyComponentId): Boolean = inner.contains(id.toLong())

    public fun indexOf(id: GearyComponentId): Int = inner.indexOf(id.toLong())

    public fun first(): GearyComponentId = inner.firstLong().toULong()
    public fun last(): GearyComponentId = inner.lastLong().toULong()

    public inline fun forEach(run: (GearyComponentId) -> Unit) {
        val iterator = inner.iterator()
        while (iterator.hasNext()) {
            run(iterator.nextLong().toULong())
        }
    }

    public inline fun any(predicate: (GearyComponentId) -> Boolean): Boolean {
        forEach { if (predicate(it)) return true }
        return false
    }

    public inline fun forEachIndexed(run: (Int, GearyComponentId) -> Unit) {
        val iterator = inner.iterator()
        var i = 0
        forEach { run(i++, iterator.nextLong().toULong()) }
    }

    public inline fun filter(predicate: (GearyComponentId) -> Boolean): GearyType {
        val type = LongAVLTreeSet()
        forEach { if (predicate(it)) type.add(it.toLong()) }
        return GearyType(type)
    }

    public operator fun plus(id: GearyComponentId): GearyType =
        GearyType(LongAVLTreeSet(inner).apply { add(id.toLong()) })

    public operator fun minus(id: GearyComponentId): GearyType =
        GearyType(LongAVLTreeSet(inner).apply { remove(id.toLong()) })
}
