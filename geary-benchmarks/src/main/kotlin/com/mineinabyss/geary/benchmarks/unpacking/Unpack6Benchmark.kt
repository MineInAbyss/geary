package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class Unpack6Benchmark {

    @Setup
    fun setUp() {
        geary(TestEngineModule)

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
            comp1
        }
    }

    @Benchmark
    fun unpack6of6Comp() {
        systemOf6().forEach {
            comp1
            comp2
            comp3
            comp4
            comp5
            comp6
        }
    }

    @Benchmark
    fun unpack1of6CompNoDelegate() {
        systemOf6WithoutDelegate().forEach {
            comp1()
        }
    }

    // This test gives ridiculous numbers, I think kotlin might just be optimizing some calls away that it can't with a delegate?
    @Benchmark
    fun unpack6of6CompNoDelegate() {
        systemOf6WithoutDelegate().forEach {
            comp1()
            comp2()
            comp3()
            comp4()
            comp5()
            comp6()
        }
    }
}

fun main() {
    Unpack6Benchmark().apply {
        setUp()
        repeat(100) {
            unpack6of6Comp()
//            unpack6of6CompNoDelegate()
        }
    }
}
