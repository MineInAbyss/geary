package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.modules.WorldScoped
import com.mineinabyss.geary.systems.query.query

fun WorldScoped.systemOf1() = cache(query<Comp1>())
fun WorldScoped.systemOf1OrNull() = cache(query<Comp1?>())
fun WorldScoped.systemOf2() = cache(query<Comp1, Comp2>())
fun WorldScoped.systemOf6() = cache(query<Comp1, Comp2, Comp3, Comp4, Comp5, Comp6>())
