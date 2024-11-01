package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
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

    @Benchmark
    fun unpack1of6Comp() {
        systemOf6().forEach { (a) ->
        }
    }

    @Benchmark
    fun unpack6of6Comp() {
        systemOf6().forEach { (a, b, c, d, e, f) ->
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
