package com.mineinabyss.geary.benchmarks.instantiation

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.benchmarks.unpacking.*
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.di.DI
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 0)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
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
    fun create10MilEntitiesWith0Components() {
        repeat(tenMil) {
            entity()
        }
    }

    @Benchmark
    fun create1MilEntitiesWith1ComponentNoEvent() {
        repeat(tenMil) {
            entity {
                set(Comp1(0), noEvent = true)
            }
        }
    }

    @Benchmark
    fun create1MilEntitiesWith1ComponentYesEvent() {
        repeat(tenMil) {
            entity {
                set(Comp1(0))
            }
        }
    }

    @Benchmark
    fun create1MilEntitiesWith6Components() {
        repeat(tenMil) {
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
    NewEntityBenchmark().create10MilEntitiesWith0Components()
}
