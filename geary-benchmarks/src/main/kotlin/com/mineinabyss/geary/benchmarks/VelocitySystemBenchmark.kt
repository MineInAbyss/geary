package com.mineinabyss.geary.benchmarks

import com.mineinabyss.geary.benchmarks.helpers.GearyBenchmark
import com.mineinabyss.geary.benchmarks.helpers.oneMil
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.query
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class VelocitySystemBenchmark : GearyBenchmark() {
    data class Velocity(val x: Float, val y: Float)
    data class Position(var x: Float, var y: Float)

    fun createVelocitySystem() = system(object : Query(this) {
        val velocity by get<Velocity>()
        var position by get<Position>()
    }).exec {
        it.position.x += it.velocity.x
        it.position.y += it.velocity.y
    }

    fun createVelocitySystemNoDelegates() = system(
        query<Velocity, Position>()
    ).exec { (velocity, position) ->
        position.x += velocity.x
        position.y += velocity.y
    }

    val velocities = Array(tenMil) { Velocity(it.toFloat() / oneMil, it.toFloat() / oneMil) }
    val positions = Array(tenMil) { Position(0f, 0f) }

    @Setup
    fun setUp() {
        repeat(tenMil) {
            entity {
                set(Velocity(it.toFloat() / oneMil, it.toFloat() / oneMil))
                set(Position(0f, 0f))
            }
        }
    }

    @Benchmark
    fun velocitySystemNoDelegates() {
        createVelocitySystemNoDelegates().tick()
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

    @Benchmark
    fun velocitySystem() {
        createVelocitySystem().tick()
    }
}

fun main() {
    VelocitySystemBenchmark().apply {
        setUp()

        repeat(400) {
            createVelocitySystem()
        }
    }
}
