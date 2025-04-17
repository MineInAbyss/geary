package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.geary.systems.query.query

fun Geary.systemOf1() = cache(query<Comp1>())
fun Geary.systemOf1OrNull() = cache(query<Comp1?>())
fun Geary.systemOf2() = cache(query<Comp1, Comp2>())
fun Geary.systemOf6() = cache(query<Comp1, Comp2, Comp3, Comp4, Comp5, Comp6>())
