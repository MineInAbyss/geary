package com.mineinabyss.geary.benchmarks.events

import com.mineinabyss.geary.benchmarks.helpers.oneMil
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.di.DI
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class EventCalls {

    var targets = emptyList<Entity>()
    @Setup(Level.Invocation)
    fun setupPerInvocation() {
        geary(TestEngineModule)
        targets = (1..oneMil).map { entity { set(it) } }
    }

    @TearDown(Level.Invocation)
    fun teardown() {
        DI.clear()
    }

    private class Listener: GearyListener() {
        val Pointers.int by get<Int>().on(target)
        val Pointers.event by get<Event>().on(event)
        var count = 0
        override fun Pointers.handle() {
            count++
        }
    }

    private class Event

    @Benchmark
    fun callEventOn1MillionEntities() {
        val event = entity {
            set(Event())
        }
        repeat(oneMil) {
            targets[it].callEvent(event)
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
