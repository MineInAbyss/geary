package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.entityTypeOf
import com.mineinabyss.geary.datatypes.family.Family
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.cId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.systems.accessors.Accessor
import com.mineinabyss.geary.systems.accessors.Accessors
import com.mineinabyss.geary.systems.accessors.ReadOnlyAccessor
import com.mineinabyss.geary.systems.accessors.ReadWriteAccessor
import kotlin.jvm.JvmName


abstract class ShorthandQuery(world: Geary, family: Family?) : Query(world, family)

class ShorthandQuery1<A1, A : ReadOnlyAccessor<A1>>(
    world: Geary,
    val accessor1: A,
    family: Family? = null,
) : ShorthandQuery(world, family) {
    override val accessors = setOf(accessor1)

    context(row: Int)
    val comp1 get() = component1()

    override fun load(archetype: Archetype) {
        accessor1.load(archetype)
    }

    context(row: Int)
    inline operator fun component1(): A1 = accessor1[row]

    inline operator fun component1(): A = accessor1
}

class ShorthandQuery2<A1, A : ReadOnlyAccessor<A1>, B1, B : ReadOnlyAccessor<B1>>(
    world: Geary,
    val accessor1: A,
    val accessor2: B,
    family: Family? = null,
) : ShorthandQuery(world, family) {
    override val accessors = setOf(accessor1, accessor2)

    context(row: Int)
    operator fun component1(): A1 = accessor1[row]

    context(row: Int)
    operator fun component2(): B1 = accessor2[row]
}

class ShorthandQuery3<A1, A : ReadOnlyAccessor<A1>, B1, B : ReadOnlyAccessor<B1>, C1, C : ReadOnlyAccessor<C1>>(
    world: Geary,
    val accessor1: A,
    val accessor2: B,
    val accessor3: C,
    family: Family? = null,
) : ShorthandQuery(world, family) {
    override val accessors = setOf(accessor1, accessor2, accessor3)

    context(row: Int)
    operator fun component1(): A1 = accessor1[row]

    context(row: Int)
    operator fun component2(): B1 = accessor2[row]

    context(row: Int)
    operator fun component3(): C1 = accessor3[row]
}

class ShorthandQuery4<A1, A : ReadOnlyAccessor<A1>, B1, B : ReadOnlyAccessor<B1>, C1, C : ReadOnlyAccessor<C1>, D1, D : ReadOnlyAccessor<D1>>(
    world: Geary,
    val accessor1: A,
    val accessor2: B,
    val accessor3: C,
    val accessor4: D,
    family: Family? = null,
) : ShorthandQuery(world, family) {
    override val accessors = setOf(accessor1, accessor2, accessor3, accessor4)

    context(row: Int)
    operator fun component1(): A1 = accessor1[row]

    context(row: Int)
    operator fun component2(): B1 = accessor2[row]

    context(row: Int)
    operator fun component3(): C1 = accessor3[row]

    context(row: Int)
    operator fun component4(): D1 = accessor4[row]
}

fun WorldScoped.query() = object : Query(world, null) {
    override val accessors = setOf<Accessor<*>>()
}

fun WorldScoped.query(match: MutableFamily.Selector.And.() -> Unit) = object : Query(world, family(match)) {
    override val accessors = setOf<Accessor<*>>()
}

fun <A1, A : ReadOnlyAccessor<A1>> WorldScoped.query(
    accessor1: A,
    filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = ShorthandQuery1(
    world, accessor1,
    entityTypeOf(),
    filterFamily?.let { family(filterFamily) }
)

inline fun <reified A> WorldScoped.query(
    size1: QueryShorthands.Size1? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = query(Accessors.getPotentiallyNullable<A>(), filterFamily)

inline fun <reified A, reified B> WorldScoped.query(
    size2: QueryShorthands.Size2? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = ShorthandQuery2<A, ReadWriteAccessor<A>, B, ReadWriteAccessor<B>>(
    world,
    Accessors.getPotentiallyNullable<A>(),
    Accessors.getPotentiallyNullable<B>(),
    filterFamily?.let { family(filterFamily) }
)


inline fun <reified A, reified B, reified C> WorldScoped.query(
    size3: QueryShorthands.Size3? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery3<A, B, C>(world) {
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

inline fun <reified A, reified B, reified C, reified D> WorldScoped.query(
    size4: QueryShorthands.Size4? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery4<A, B, C, D>(world) {
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

inline fun <reified A, reified B, reified C, reified D, reified E> WorldScoped.query(
    size5: QueryShorthands.Size5? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery5<A, B, C, D, E>(world) {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>(), cId<E>())
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

inline fun <reified A, reified B, reified C, reified D, reified E, reified F> WorldScoped.query(
    size6: QueryShorthands.Size6? = null,
    noinline filterFamily: (MutableFamily.Selector.And.() -> Unit)? = null,
) = object : ShorthandQuery6<A, B, C, D, E, F>(world) {
    override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>(), cId<D>(), cId<E>(), cId<F>())
    override fun ensure() {
        filterFamily?.let { this { it() } }
    }

    private val accessor1 = getPotentiallyNullable<A>()
    private val accessor2 = getPotentiallyNullable<B>()
    private val accessor3 = getPotentiallyNullable<C>()
    private val accessor4 = getPotentiallyNullable<D>()
    private val accessor5 = getPotentiallyNullable<E>()
    private val accessor6 = getPotentiallyNullable<F>()

    override fun component1(): A = accessor1.get(this)
    override fun component2(): B = accessor2.get(this)
    override fun component3(): C = accessor3.get(this)
    override fun component4(): D = accessor4.get(this)
    override fun component5(): E = accessor5.get(this)
    override fun component6(): F = accessor6.get(this)
}

@JvmName("toList1")
inline fun <T> CachedQuery<ShorthandQuery1<T, *>>.toList(): List<T> = map { it.component1() }

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
    sealed class Size6
}
