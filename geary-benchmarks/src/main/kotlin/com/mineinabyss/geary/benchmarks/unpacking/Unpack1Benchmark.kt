package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.ITERATIONS
import com.mineinabyss.geary.benchmarks.helpers.WARMUP_ITERATIONS
import com.mineinabyss.geary.benchmarks.helpers.tenMil
import com.mineinabyss.geary.datatypes.GearyRecord
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary

import com.mineinabyss.geary.systems.query.GearyQuery
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = WARMUP_ITERATIONS)
@Measurement(iterations = ITERATIONS, time = 1, timeUnit = TimeUnit.SECONDS)
class Unpack1Benchmark {
    object SystemOf1 : GearyQuery() {
        val GearyRecord.comp1 by get<Comp1>()
    }

    @Setup
    fun setUp() {
        geary(TestEngineModule)

        repeat(tenMil) {
            entity {
                set(Comp1(1))
            }
        }
    }

    // 5.149 ops/s
    @Benchmark
    fun unpack1of1Comp() {
        SystemOf1.run {
            fastForEach {
                it.comp1
            }
        }
    }
}
