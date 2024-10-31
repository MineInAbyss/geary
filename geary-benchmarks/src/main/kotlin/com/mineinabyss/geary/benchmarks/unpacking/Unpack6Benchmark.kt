package com.mineinabyss.geary.benchmarks.unpacking

import GearyBenchmark
import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.helpers.entity
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
        systemOf1().forEach {
            it.comp1
        }
    }

    @Benchmark
    fun unpack1of6CompWithUnoptimizedAccessor() {
        systemOf1Defaulting().forEach {
            it.comp1
        }
    }

    @Benchmark
    fun unpack6of6Comp() {
        systemOf6().forEach {
            it.comp1
            it.comp2
            it.comp3
            it.comp4
            it.comp5
            it.comp6
        }
    }

    @Benchmark
    fun unpack1of6CompNoDelegate() {
        systemOf6WithoutDelegate().forEach {
            it.comp1.get(it)
        }
    }

    // This test gives ridiculous numbers, I think kotlin might just be optimizing some calls away that it can't with a delegate?
    @Benchmark
    fun unpack6of6CompNoDelegate() {
        systemOf6WithoutDelegate().forEach {
            it.comp1.get(it)
            it.comp2.get(it)
            it.comp3.get(it)
            it.comp4.get(it)
            it.comp5.get(it)
            it.comp6.get(it)
        }
    }
}

fun main() {
    Unpack6Benchmark().apply {
        setUp()
        repeat(10000) {
//            unpack1of6CompNoDelegate()
            unpack6of6CompNoDelegate()
        }
    }
}
