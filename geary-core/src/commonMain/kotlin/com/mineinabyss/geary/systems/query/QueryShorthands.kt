package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.entityTypeOf
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.cId
import com.mineinabyss.geary.modules.Geary
import kotlin.jvm.JvmName


abstract class ShorthandQuery(world: Geary) : Query(world) {
    abstract val involves: EntityType
}

abstract class ShorthandQuery1<A>(world: Geary) : ShorthandQuery(world) {
    val comp1 get() = component1()

    abstract operator fun component1(): A
}

abstract class ShorthandQuery2<A, B>(world: Geary) : ShorthandQuery(world) {
    val comp1 get() = component1()
    val comp2 get() = component2()

    abstract operator fun component1(): A
    abstract operator fun component2(): B
}

abstract class ShorthandQuery3<A, B, C>(world: Geary) : ShorthandQuery(world) {
    val comp1 get() = component1()
    val comp2 get() = component2()
    val comp3 get() = component3()

    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
}

abstract class ShorthandQuery4<A, B, C, D>(world: Geary) : ShorthandQuery(world) {
    val comp1 get() = component1()
    val comp2 get() = component2()
    val comp3 get() = component3()
    val comp4 get() = component4()

    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
    abstract operator fun component4(): D
}

abstract class ShorthandQuery5<A, B, C, D, E>(world: Geary) : ShorthandQuery(world) {
    val comp1 get() = component1()
    val comp2 get() = component2()
    val comp3 get() = component3()
    val comp4 get() = component4()
    val comp5 get() = component5()

    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
    abstract operator fun component4(): D
    abstract operator fun component5(): E
}


fun Geary.query() = object : Query(this) {}

fun Geary.query(match: MutableFamily.Selector.And.() -> Unit) = object : Query(this) {
    override fun ensure() = this { add(family(match)) }
}

inline fun <reified A> Geary.query(
    size1: QueryShorthands.Size1? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null
) = object : ShorthandQuery1<A>(this) {
    override val involves = entityTypeOf(cId<A>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    private val accessor1 = getPotentiallyNullable<A>()

    override fun component1() = accessor1.get(this)
}

inline fun <reified A, reified B> Geary.query(
    size2: QueryShorthands.Size2? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery2<A, B>(this) {
    override val involves = entityTypeOf(cId<A>(), cId<B>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    private val accessor1 = getPotentiallyNullable<A>()
    private val accessor2 = getPotentiallyNullable<B>()

    override fun component1(): A = accessor1.get(this)
    override fun component2(): B = accessor2.get(this)
}


inline fun <reified A, reified B, reified C> Geary.query(
    size3: QueryShorthands.Size3? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery3<A, B, C>(this) {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    private val accessor1 = getPotentiallyNullable<A>()
    private val accessor2 = getPotentiallyNullable<B>()
    private val accessor3 = getPotentiallyNullable<C>()

    override fun component1(): A = accessor1.get(this)
    override fun component2(): B = accessor2.get(this)
    override fun component3(): C = accessor3.get(this)
}

inline fun <reified A, reified B, reified C, reified D> Geary.query(
    size4: QueryShorthands.Size4? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery4<A, B, C, D>(this) {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    private val accessor1 = getPotentiallyNullable<A>()
    private val accessor2 = getPotentiallyNullable<B>()
    private val accessor3 = getPotentiallyNullable<C>()
    private val accessor4 = getPotentiallyNullable<D>()

    override fun component1(): A = accessor1.get(this)
    override fun component2(): B = accessor2.get(this)
    override fun component3(): C = accessor3.get(this)
    override fun component4(): D = accessor4.get(this)
}

inline fun <reified A, reified B, reified C, reified D, reified E> Geary.query(
    size5: QueryShorthands.Size5? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery5<A, B, C, D, E>(this) {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    private val accessor1 = getPotentiallyNullable<A>()
    private val accessor2 = getPotentiallyNullable<B>()
    private val accessor3 = getPotentiallyNullable<C>()
    private val accessor4 = getPotentiallyNullable<D>()
    private val accessor5 = getPotentiallyNullable<E>()

    override fun component1(): A = accessor1.get(this)
    override fun component2(): B = accessor2.get(this)
    override fun component3(): C = accessor3.get(this)
    override fun component4(): D = accessor4.get(this)
    override fun component5(): E = accessor5.get(this)
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
