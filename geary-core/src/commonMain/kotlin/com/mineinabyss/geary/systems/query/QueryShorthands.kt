package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.entityTypeOf
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.cId
import kotlin.jvm.JvmName


abstract class ShorthandQuery : Query() {
    abstract val involves: EntityType
}

abstract class ShorthandQuery1<A> : ShorthandQuery() {
    abstract val comp1: A

    abstract operator fun component1(): A
}

abstract class ShorthandQuery2<A, B> : ShorthandQuery() {
    abstract val comp1: A
    abstract val comp2: B

    abstract operator fun component1(): A
    abstract operator fun component2(): B
}

abstract class ShorthandQuery3<A, B, C> : ShorthandQuery() {
    abstract val comp1: A
    abstract val comp2: B
    abstract val comp3: C

    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
}

abstract class ShorthandQuery4<A, B, C, D> : ShorthandQuery() {
    abstract val comp1: A
    abstract val comp2: B
    abstract val comp3: C
    abstract val comp4: D

    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
    abstract operator fun component4(): D
}

abstract class ShorthandQuery5<A, B, C, D, E> : ShorthandQuery() {
    abstract val comp1: A
    abstract val comp2: B
    abstract val comp3: C
    abstract val comp4: D
    abstract val comp5: E

    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
    abstract operator fun component4(): D
    abstract operator fun component5(): E
}


fun query() = object : Query() {}

fun query(match: MutableFamily.Selector.And.() -> Unit) = object : Query() {
    override fun ensure() = this { add(family(match)) }
}

inline fun <reified A> query(
    size1: QueryShorthands.Size1? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null
) = object : ShorthandQuery1<A>() {
    override val involves = entityTypeOf(cId<A>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    override val comp1 by getPotentiallyNullable<A>()

    override fun component1() = comp1
}

inline fun <reified A, reified B> query(
    size2: QueryShorthands.Size2? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery2<A, B>() {
    override val involves = entityTypeOf(cId<A>(), cId<B>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    override val comp1 by getPotentiallyNullable<A>()
    override val comp2 by getPotentiallyNullable<B>()

    override fun component1(): A = comp1
    override fun component2(): B = comp2
}


inline fun <reified A, reified B, reified C> query(
    size3: QueryShorthands.Size3? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery3<A, B, C>() {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    override val comp1 by getPotentiallyNullable<A>()
    override val comp2 by getPotentiallyNullable<B>()
    override val comp3 by getPotentiallyNullable<C>()

    override fun component1(): A = comp1
    override fun component2(): B = comp2
    override fun component3(): C = comp3
}

inline fun <reified A, reified B, reified C, reified D> query(
    size4: QueryShorthands.Size4? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery4<A, B, C, D>() {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    override val comp1 by getPotentiallyNullable<A>()
    override val comp2 by getPotentiallyNullable<B>()
    override val comp3 by getPotentiallyNullable<C>()
    override val comp4 by getPotentiallyNullable<D>()

    override fun component1(): A = comp1
    override fun component2(): B = comp2
    override fun component3(): C = comp3
    override fun component4(): D = comp4
}

inline fun <reified A, reified B, reified C, reified D, reified E> query(
    size5: QueryShorthands.Size5? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery5<A, B, C, D, E>() {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    override val comp1 by getPotentiallyNullable<A>()
    override val comp2 by getPotentiallyNullable<B>()
    override val comp3 by getPotentiallyNullable<C>()
    override val comp4 by getPotentiallyNullable<D>()
    override val comp5 by getPotentiallyNullable<E>()

    override fun component1(): A = comp1
    override fun component2(): B = comp2
    override fun component3(): C = comp3
    override fun component4(): D = comp4
    override fun component5(): E = comp5
}

@JvmName("toList1")
inline fun <T> CachedQuery<ShorthandQuery1<T>>.toList(): List<T> = map { it.component1() }

@JvmName("toList2")
inline fun <T, R> CachedQuery<ShorthandQuery2<T, R>>.toList(): List<Pair<T, R>> =
    map { it.component1() to it.component2() }


object QueryShorthands {
    // Kotlin runs into conflicting overloads if we don't specify the defaulting size parameter.
    // The extra sealed classes are a workaround.
    sealed class Size1
    sealed class Size2
    sealed class Size3
    sealed class Size4
    sealed class Size5
}
