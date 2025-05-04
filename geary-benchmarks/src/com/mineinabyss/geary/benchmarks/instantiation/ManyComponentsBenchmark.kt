package com.mineinabyss.geary.benchmarks.instantiation

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
class ManyComponentsBenchmark {
    @Setup
    fun setLoggingLevel() {
        Logger.setMinSeverity(Severity.Warn)
    }

    var geary: Geary = geary(TestEngineModule).start()

    @Setup(Level.Invocation)
    fun setupPerInvocation() {
        geary = geary(TestEngineModule).start()
    }

    @Benchmark
    fun createTenThousandEntitiesWithUniqueComponentEach() = with(geary) {
        repeat(10000) {
            entity {
                addRelation<String>(it.toLong().toGeary())
            }
        }
    }
}

fun main() {
    geary(TestEngineModule)
    ManyComponentsBenchmark().createTenThousandEntitiesWithUniqueComponentEach()
}
