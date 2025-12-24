package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.helpers.entity
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class Unpack4Benchmark : GearyBenchmark() {
    @Setup
    fun setUp() {
        repeat(tenMil) {
            entity {
                set(Comp1(1))
                set(Comp2(1))
                set(Comp3(1))
                set(Comp4(1))
            }
        }
    }

    @Benchmark
    fun unpack4Comp() {
        systemOf4().forEach { (a, b, c, d) -> }
    }
}
