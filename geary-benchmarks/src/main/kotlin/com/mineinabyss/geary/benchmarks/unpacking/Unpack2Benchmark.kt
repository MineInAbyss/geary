package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.Comp1
import com.mineinabyss.geary.benchmarks.helpers.Comp2
import com.mineinabyss.geary.benchmarks.helpers.GearyBenchmark
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.helpers.entity
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class Unpack2Benchmark : GearyBenchmark() {
    @Setup
    fun setUp() {
        repeat(tenMil) {
            entity {
                set(Comp1(1))
                set(Comp2(1))
            }
        }
    }

    @Benchmark
    fun unpack1of2Comp() {
        systemOf2().forEach { (a) -> }
    }

    @Benchmark
    fun unpack2of2Comp() {
        systemOf2().forEach { (a, b) -> }
    }
}
