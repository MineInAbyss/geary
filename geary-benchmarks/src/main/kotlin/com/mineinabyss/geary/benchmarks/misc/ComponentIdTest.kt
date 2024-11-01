package com.mineinabyss.geary.benchmarks.misc

import com.mineinabyss.geary.benchmarks.helpers.GearyBenchmark
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import kotlin.reflect.typeOf

@State(Scope.Benchmark)
class ComponentIdTest : GearyBenchmark() {

    @Setup
    fun setup() {
        Logger.setMinSeverity(Severity.Warn)
        geary(TestEngineModule)
    }

    @Benchmark
    fun getKType() {
        repeat(tenMil) {
            typeOf<Comp1>()
        }
    }

    @Benchmark
    fun getKClass() {
        repeat(tenMil) {
            typeOf<Comp1>().classifier
        }
    }

    @Benchmark
    fun componentIdFor6Comp() {
        repeat(tenMil) {
            componentId<Comp1>()
            componentId<Comp2>()
            componentId<Comp3>()
            componentId<Comp4>()
            componentId<Comp5>()
            componentId<Comp6>()
        }
    }
}

fun main() {
    geary(TestEngineModule)
    ComponentIdTest().apply {
        setup()
        repeat(10) {
            componentIdFor6Comp()
        }
    }
}
