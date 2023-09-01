package com.mineinabyss.geary.benchmarks.instantiation

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.di.DI
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class NewEntityBenchmark {
    @Setup
    fun setLoggingLevel() {
        Logger.setMinSeverity(Severity.Warn)
    }

    @Setup(Level.Invocation)
    fun setupPerInvocation() {
        geary(TestEngineModule)
    }

    @TearDown(Level.Invocation)
    fun teardown() {
        DI.clear()
    }

    @Benchmark
    fun create1MilEntitiesWith0Components() {
        repeat(oneMil) {
            entity()
        }
    }

    @Benchmark
    fun create1MilEntitiesWith1ComponentNoEvent() {
        repeat(oneMil) {
            entity {
                set(Comp1(0), noEvent = true)
            }
        }
    }

    @Benchmark
    fun create1MilEntitiesWith1ComponentYesEvent() {
        repeat(oneMil) {
            entity {
                set(Comp1(0))
            }
        }
    }

    @Benchmark
    fun create1MilEntitiesWith6Components() {
        repeat(oneMil) {
            entity {
                set(Comp1(0), noEvent = true)
                set(Comp2(0), noEvent = true)
                set(Comp3(0), noEvent = true)
                set(Comp4(0), noEvent = true)
                set(Comp5(0), noEvent = true)
                set(Comp6(0), noEvent = true)
            }
        }
    }
}

fun main() {
    geary(TestEngineModule)
    NewEntityBenchmark().create1MilEntitiesWith6Components()
}
