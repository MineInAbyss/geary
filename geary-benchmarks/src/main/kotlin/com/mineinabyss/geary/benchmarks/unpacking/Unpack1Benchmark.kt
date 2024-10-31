package com.mineinabyss.geary.benchmarks.unpacking

import GearyBenchmark
import com.mineinabyss.geary.benchmarks.helpers.Comp1
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.helpers.entity
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class Unpack1Benchmark : GearyBenchmark() {

    @Setup
    fun setUp() {
        repeat(tenMil) {
            entity {
                set(Comp1(1))
            }
        }
    }

    @Benchmark
    fun unpack1of1Comp() {
        systemOf1().forEach {
            comp1
        }
    }
}
