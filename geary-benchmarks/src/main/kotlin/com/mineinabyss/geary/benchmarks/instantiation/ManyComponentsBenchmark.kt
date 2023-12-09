package com.mineinabyss.geary.benchmarks.instantiation

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.idofront.di.DI
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class ManyComponentsBenchmark {
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
    fun create1MilEntitiesWithUniqueComponentEach() {
        repeat(10000) {
            entity {
                addRelation<String>(it.toLong().toGeary())
            }
        }
    }
}

fun main() {
    geary(TestEngineModule)
    ManyComponentsBenchmark().create1MilEntitiesWithUniqueComponentEach()
}
