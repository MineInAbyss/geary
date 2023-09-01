package com.mineinabyss.geary.benchmarks

import com.mineinabyss.geary.benchmarks.helpers.ITERATIONS
import com.mineinabyss.geary.benchmarks.helpers.WARMUP_ITERATIONS
import com.mineinabyss.geary.benchmarks.helpers.oneMil
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.datatypes.GearyRecord
import com.mineinabyss.geary.datatypes.Record
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.RepeatingSystem
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = WARMUP_ITERATIONS)
@Measurement(iterations = ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class VelocitySystemBenchmark {
    data class Velocity(val x: Float, val y: Float)
    data class Position(val x: Float, val y: Float)

    object VelocitySystem : RepeatingSystem() {
        private val GearyRecord.velocity by get<Velocity>()
        private var GearyRecord.position by get<Position>()

        override fun Record.tick() {
            position = Position(position.x + velocity.x, position.y + velocity.y)
        }
    }

    @Setup
    fun setUp() {
        geary(TestEngineModule)

        repeat(tenMil) {
            entity {
                set(Velocity(it.toFloat() / oneMil, it.toFloat() / oneMil))
                set(Position(0f, 0f))
            }
        }
    }

    // 0.606 ops/s
    @Benchmark
    fun velocitySystem() {
        VelocitySystem.doTick()
    }
}

fun main() {
    VelocitySystemBenchmark().apply {
        setUp()
        velocitySystem()
    }
}