package com.mineinabyss.geary.systems.query

import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.datatypes.family.family


abstract class ShorthandQuery1<A> : Query() {
    abstract operator fun component1(): A
}

abstract class ShorthandQuery2<A, B> : Query() {
    abstract operator fun component1(): A
    abstract operator fun component2(): B
}

abstract class ShorthandQuery3<A, B, C> : Query() {
    abstract operator fun component1(): A
    abstract operator fun component2(): B
    abstract operator fun component3(): C
}


open class QueryShorthands {
    // Kotlin runs into conflicting overloads if we don't specify the defaulting size parameter.
    // The extra sealed classes are a workaround.
    sealed class Size1
    sealed class Size2
    sealed class Size3

    fun any() = object : Query() { }
    inline fun <reified A : Any> of(size1: Size1? = null) = object : ShorthandQuery1<A>() {
        val comp1 by get<A>()

        override fun component1() = comp1
    }

    inline fun <reified A : Any, reified B : Any> of(size2: Size2? = null) = object : ShorthandQuery2<A, B>() {
        val comp1 by get<A>()
        val comp2 by get<B>()

        override fun component1(): A = comp1
        override fun component2(): B = comp2
    }

    inline fun <reified A : Any, reified B : Any, reified C : Any> of(size3: Size3? = null) =
        object : ShorthandQuery3<A, B, C>() {
            val comp1 by get<A>()
            val comp2 by get<B>()
            val comp3 by get<C>()

            override fun component1(): A = comp1
            override fun component2(): B = comp2
            override fun component3(): C = comp3
        }

    fun of(match: MutableFamily.Selector.And.() -> Unit) = object : Query() {
        override fun ensure() = this { add(family(match)) }
    }
}
