package test.unpacking

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
class Unpack6Benchmark {
    object SystemOf6 : GearyQuery() {
        val GearyRecord.int by get<Int>()
        val GearyRecord.double by get<Double>()
        val GearyRecord.float by get<Float>()
        val GearyRecord.byte by get<Byte>()
        val GearyRecord.short by get<Short>()
        val GearyRecord.long by get<Long>()
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
                set(1)
                set(1.0)
                set(1f)
                set(1.toByte())
                set(1.toShort())
                set(1L)
            }
        }
    }

    // 4.890 ops/s
    @Benchmark
    fun unpack1of6Comp() {
        OneSystem.run {
            fastForEach {
                it.int
            }
        }
    }

    // 1.749 ops/s
    @Benchmark
    fun unpack6of6Comp() {
        SystemOf6.run {
            fastForEach {
                it.int
                it.double
                it.float
                it.byte
                it.short
                it.long
            }
        }
    }
}
