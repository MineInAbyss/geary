package com.mineinabyss.geary.benchmarks

import com.mineinabyss.geary.benchmarks.helpers.oneMil
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.system
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class VelocitySystemBenchmark {
    data class Velocity(val x: Float, val y: Float)
    data class Position(var x: Float, var y: Float)

    val VelocitySystem = geary.system(object : Query() {
        val velocity by target.get<Velocity>()
        var position by target.get<Position>()
    }) {
        onTick {
            position.x += velocity.x
            position.y += velocity.y
        }
    }

    val velocities = Array(tenMil) { Velocity(it.toFloat() / oneMil, it.toFloat() / oneMil) }
    val positions = Array(tenMil) { Position(0f, 0f) }

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

    @Benchmark
    fun velocitySystem() {
        createVelocitySystem().tickAll()
    }

    // Theoretical performance with zero ECS overhead
    @Benchmark
    fun pureArrays() {
        var i = 0
        while (i < tenMil) {
            positions[i].x += velocities[i].x
            positions[i].y += velocities[i].y
            i++
        }
    }
}

fun main() {
    VelocitySystemBenchmark().apply {
        setUp()

        repeat(400) {
//            velocitySystem()
        }
    }
}
