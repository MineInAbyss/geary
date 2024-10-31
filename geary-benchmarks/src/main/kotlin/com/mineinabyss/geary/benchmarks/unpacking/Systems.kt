package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.systems.query.GearyQuery

class Query1(world: Geary) : GearyQuery(world) {
    val comp1 by get<Comp1>()
}

class Query1Defaulting(world: Geary) : GearyQuery(world) {
    val comp1 by get<Comp1>().orDefault { Comp1(0) }
    override fun ensure() = this { has<Comp1>() }
}

class Query2(world: Geary) : GearyQuery(world) {
    val comp1 by get<Comp1>()
    val comp2 by get<Comp2>()
}

class Query6(world: Geary) : GearyQuery(world) {
    val comp1 by get<Comp1>()
    val comp2 by get<Comp2>()
    val comp3 by get<Comp3>()
    val comp4 by get<Comp4>()
    val comp5 by get<Comp5>()
    val comp6 by get<Comp6>()
}


class Query6WithoutDelegate(world: Geary) : GearyQuery(world) {
    val comp1 = get<Comp1>()
    val comp2 = get<Comp2>()
    val comp3 = get<Comp3>()
    val comp4 = get<Comp4>()
    val comp5 = get<Comp5>()
    val comp6 = get<Comp6>()

    override fun ensure() = this {
        hasSet<Comp1>()
        hasSet<Comp2>()
        hasSet<Comp3>()
        hasSet<Comp4>()
        hasSet<Comp5>()
        hasSet<Comp6>()
    }
}

fun Geary.systemOf1() = cache(::Query1)
fun Geary.systemOf1Defaulting() = cache(::Query1Defaulting)
fun Geary.systemOf2() = cache(::Query2)
fun Geary.systemOf6() = cache(::Query6)
fun Geary.systemOf6WithoutDelegate() = cache(::Query6WithoutDelegate)
