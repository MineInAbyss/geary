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
@Measurement(iterations = 0, time = 1, timeUnit = TimeUnit.SECONDS)
class Unpack1Benchmark {
    object SystemOf1 : GearyQuery() {
        val GearyRecord.int by get<Int>()
    }

    val oneMil = 1000000
    val tenMil = 10000000

    @Setup
    fun setUp() {
        geary(TestEngineModule)

        repeat(tenMil) {
            entity {
                set(1)
            }
        }
    }

    // 5.149 ops/s
    @Benchmark
    fun unpack1of1Comp() {
        SystemOf1.run {
            fastForEach {
                it.int
            }
        }
    }
}
