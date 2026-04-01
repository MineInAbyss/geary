package com.mineinabyss.geary.benchmarks.events

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
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
    var geary: Geary = geary(TestEngineModule)

    init {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Setup(Level.Invocation)
    fun setupPerInvocation() {
        geary = geary(TestEngineModule)
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
//    val types = mutableMapOf<Class<*>, Long>()
//    geary(TestEngineModule)
//    repeat(oneMil) { String::class.java }
//    repeat(oneMil) { types[String::class.java] }
    EventCalls().apply {
        setupPerInvocation()
        repeat(1000) {
            callEventOn1MillionEntities()
        }
    }
}
