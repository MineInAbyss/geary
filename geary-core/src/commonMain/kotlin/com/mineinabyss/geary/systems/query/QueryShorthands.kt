package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.entityTypeOf
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.cId


abstract class ShorthandQuery : Query() {
    abstract val involves: EntityType
}

abstract class ShorthandQuery1<A> : ShorthandQuery() {
    abstract operator fun component1(): A
}

abstract class ShorthandQuery2<A, B> : ShorthandQuery() {
    abstract operator fun component1(): A
    abstract operator fun component2(): B
}

abstract class ShorthandQuery3<A, B, C> : ShorthandQuery() {
    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
}

fun query() = object : Query() {}

fun query(match: MutableFamily.Selector.And.() -> Unit) = object : Query() {
    override fun ensure() = this { add(family(match)) }
}

inline fun <reified A : Any> query(size1: QueryShorthands.Size1? = null) =
    object : ShorthandQuery1<A>() {
        override val involves = entityTypeOf(cId<A>())

        val comp1 by get<A>()

        override fun component1() = comp1
    }

inline fun <reified A : Any, reified B : Any> query(size2: QueryShorthands.Size2? = null) =
    object : ShorthandQuery2<A, B>() {
        override val involves = entityTypeOf(cId<A>(), cId<B>())

        val comp1 by get<A>()
        val comp2 by get<B>()

        override fun component1(): A = comp1
        override fun component2(): B = comp2
    }

inline fun <reified A : Any, reified B : Any, reified C : Any> query(size3: QueryShorthands.Size3? = null) =
    object : ShorthandQuery3<A, B, C>() {
        override val involves = entityTypeOf(cId<A>(), cId<B>(), cId<C>())

        val comp1 by get<A>()
        val comp2 by get<B>()
        val comp3 by get<C>()

        override fun component1(): A = comp1
        override fun component2(): B = comp2
        override fun component3(): C = comp3
    }


object QueryShorthands {
    // Kotlin runs into conflicting overloads if we don't specify the defaulting size parameter.
    // The extra sealed classes are a workaround.
    sealed class Size1
    sealed class Size2
    sealed class Size3
}
