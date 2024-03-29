package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.Comp1
import com.mineinabyss.geary.benchmarks.helpers.Comp2
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class Unpack2Benchmark {
    private object SystemOf2 : GearyQuery() {
        val Pointer.comp1 by get<Comp1>()
        val Pointer.comp2 by get<Comp2>()
    }

    private object SystemOf1 : GearyQuery() {
        val Pointer.comp1 by get<Comp1>()
    }

    @Setup
    fun setUp() {
        geary(TestEngineModule) {
        }

        repeat(tenMil) {
            entity {
                set(Comp1(1))
                set(Comp2(1))
            }
        }
    }

    @Benchmark
    fun unpack1of2Comp() {
        SystemOf1.run {
            forEach {
                it.comp1
            }
        }
    }

    @Benchmark
    fun unpack2of2Comp() {
        SystemOf2.run {
            forEach {
                it.comp1
                it.comp2
            }
        }
    }
}
