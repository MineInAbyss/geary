package com.mineinabyss.geary.benchmarks.events

import com.mineinabyss.geary.benchmarks.helpers.oneMil
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.modules.observe
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class EventCalls {
    private class TestEvent

    var targets = emptyList<Entity>()
    var geary: Geary = geary(TestEngineModule).start()

    @Setup(Level.Invocation)
    fun setupPerInvocation() {
        geary = geary(TestEngineModule).start()
        targets = (1..oneMil).map { geary.entity().apply { set(it) } }
        createListener()
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
        repeat(1000) {
            callEventOn1MillionEntities()
        }
    }
}
