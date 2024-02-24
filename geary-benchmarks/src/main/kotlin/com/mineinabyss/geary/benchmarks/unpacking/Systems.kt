package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.cachedQuery
import com.mineinabyss.geary.systems.query.GearyQuery

class Query1 : GearyQuery() {
    val comp1 by get<Comp1>()
}

class Query2 : GearyQuery() {
    val comp1 by get<Comp1>()
    val comp2 by get<Comp2>()
}

class Query6 : GearyQuery() {
    val comp1 by get<Comp1>()
    val comp2 by get<Comp2>()
    val comp3 by get<Comp3>()
    val comp4 by get<Comp4>()
    val comp5 by get<Comp5>()
    val comp6 by get<Comp6>()
}


class Query6WithoutDelegate : GearyQuery() {
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

fun systemOf1() = geary.cachedQuery(Query1())
fun systemOf2() = geary.cachedQuery(Query2())
fun systemOf6() = geary.cachedQuery(Query6())
fun systemOf6WithoutDelegate() = geary.cachedQuery(Query6WithoutDelegate())
