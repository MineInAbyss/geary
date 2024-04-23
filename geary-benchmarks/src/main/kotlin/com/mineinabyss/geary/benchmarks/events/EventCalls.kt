package com.mineinabyss.geary.benchmarks.events

import com.mineinabyss.geary.benchmarks.helpers.oneMil
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.idofront.di.DI
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class EventCalls {
    private class TestEvent

    var targets = emptyList<Entity>()

    @Setup(Level.Invocation)
    fun setupPerInvocation() {
        geary(TestEngineModule)
        targets = (1..oneMil).map { entity { set(it) } }
        createListener()
    }

    @TearDown(Level.Invocation)
    fun teardown() {
        DI.clear()
    }

    var count = 0

    fun createListener() = geary.observe<TestEvent>().exec {
        count++
    }

    @Benchmark
    fun callEventOn1MillionEntities() {
        repeat(oneMil) {
            targets[it].emit<TestEvent>()
        }
    }
}

fun main() {
    geary(TestEngineModule)
    EventCalls().apply {
        setupPerInvocation()
        repeat(100) {
            callEventOn1MillionEntities()
        }
    }
}
