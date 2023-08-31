package com.mineinabyss.geary.benchmarks.instantiation

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.engine.archetypes.Archetype
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 0)
@Measurement(iterations = 0, time = 1, timeUnit = TimeUnit.SECONDS)
class ArchetypeBenchmark {
    var arch: Archetype = Archetype(EntityType(listOf(1uL, 1uL shl 10, 1uL shl 32 or 1uL)), 0)

    @Benchmark
    fun indexOf() {
        arch.indexOf(1uL)
        arch.indexOf(2uL)
        arch.indexOf(1uL shl 32 or 1uL)
    }

    @Benchmark
    fun contains() {
        arch.contains(1uL)
        arch.contains(2uL)
        arch.contains(1uL shl 32 or 1uL)
    }
}
