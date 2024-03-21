package com.mineinabyss.geary.benchmarks.instantiation

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.helpers.componentId
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

    @Benchmark
    fun create1MilEntitiesWith6ComponentsWithoutComponentIdCalls() {
        val comp1Id = componentId<Comp1>()
        val comp2Id = componentId<Comp2>()
        val comp3Id = componentId<Comp3>()
        val comp4Id = componentId<Comp4>()
        val comp5Id = componentId<Comp5>()
        val comp6Id = componentId<Comp6>()

        repeat(oneMil) {
            entity {
                set(Comp1(0), comp1Id, noEvent = true)
                set(Comp2(0), comp2Id, noEvent = true)
                set(Comp3(0), comp3Id, noEvent = true)
                set(Comp4(0), comp4Id, noEvent = true)
                set(Comp5(0), comp5Id, noEvent = true)
                set(Comp6(0), comp6Id, noEvent = true)
            }
        }
    }
}

fun main() {
    geary(TestEngineModule)
    repeat(100) {
        NewEntityBenchmark().create1MilEntitiesWith6ComponentsWithoutComponentIdCalls()
    }
}
