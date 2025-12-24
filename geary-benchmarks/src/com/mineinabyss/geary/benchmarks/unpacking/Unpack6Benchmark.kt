package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.query.query
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class Unpack6Benchmark : GearyBenchmark() {
    @Setup
    fun setUp() {
        repeat(tenMil) {
            entity {
                set(Comp1(0))
                set(Comp2(0))
                set(Comp3(0))
                set(Comp4(0))
                set(Comp5(0))
                set(Comp6(0))
            }
        }
    }

//    @Benchmark
//    fun unpack1of6Comp() {
//        systemOf6().forEach { (a) ->
//        }
//    }

    @Benchmark
    fun unpack6of6Comp() {
        systemOf1().forEach { (a) ->
        }
        systemOf1().forEachArchetype { query ->
            query.load(this)
            forEachRow {
                val (a) = query
                query.accessor1[this] = Comp1(20)
            }
        }
        systemOf6().forEach { (a, b, c, d, e, f) ->
        }
    }

    @Benchmark
    fun unpack6of6CompByHand() {
        val comp1 = componentId<Comp1>() or HOLDS_DATA
        val comp2 = componentId<Comp2>() or HOLDS_DATA
        val comp3 = componentId<Comp3>() or HOLDS_DATA
        val comp4 = componentId<Comp4>() or HOLDS_DATA
        val comp5 = componentId<Comp5>() or HOLDS_DATA
        val comp6 = componentId<Comp6>() or HOLDS_DATA
        cache(query { has(comp1, comp2, comp3, comp4, comp5, comp6) }).forEachArchetype { _ ->
            val comp1Data = getComponentArray<Comp1>(indexOf(comp1))
            val comp2Data = getComponentArray<Comp2>(indexOf(comp2))
            val comp3Data = getComponentArray<Comp3>(indexOf(comp3))
            val comp4Data = getComponentArray<Comp4>(indexOf(comp4))
            val comp5Data = getComponentArray<Comp5>(indexOf(comp5))
            val comp6Data = getComponentArray<Comp6>(indexOf(comp6))
            forEachRow {
                val a = comp1Data[this]
                val b = comp2Data[this]
                val c = comp3Data[this]
                val d = comp4Data[this]
                val e = comp5Data[this]
                val f = comp6Data[this]
            }
        }
    }
}

fun main() {
    Unpack6Benchmark().apply {
        setUp()
        val query = cache(query<Comp1, Comp2, Comp3, Comp4, Comp5, Comp6>())
        repeat(10000) {
            query.forEach { (a, b, c, d, e, f) ->
            }
        }
    }
}
