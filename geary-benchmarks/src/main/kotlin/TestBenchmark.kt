package test

import com.mineinabyss.geary.datatypes.GearyRecord
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary

import com.mineinabyss.geary.systems.query.GearyQuery
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 0)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
class TestBenchmark {
    object BothSystem : GearyQuery() {
        val GearyRecord.int by get<Int>()
        val GearyRecord.double by get<Double>()
    }

    object OneSystem : GearyQuery() {
        val GearyRecord.int by get<Int>()
    }

    val oneMil = 1000000
    val tenMil = 10000000

    @Setup
    fun setUp() {
        geary(TestEngineModule) {
        }

        repeat(tenMil) {
            entity {
                set(1.0)
                set(1)
            }
        }
    }

    // 29.989 ops/s
    // 49.31 ops/s AFTER
    @Benchmark
    fun unpack1of2Comp() {
        OneSystem.run {
            fastForEach {
                it.int
            }
        }
    }

    // 20.373 ops/s
    // 33.436 ops/s AFTER
    @Benchmark
    fun unpack2of2Comp() {
        BothSystem.run {
            fastForEach {
                it.int
                it.double
            }
        }
    }
}
