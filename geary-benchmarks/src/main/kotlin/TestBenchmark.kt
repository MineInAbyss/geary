package test

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

//@State(Scope.Benchmark)
//@Fork(1)
//@Warmup(iterations = 0)
//@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
class TestBenchmark {
    object MySystem : GearyQuery() {
        val TargetScope.int by get<Int>()
        val TargetScope.double by get<Double>()
    }

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

//    @Benchmark
    fun unpack1Comp() {
        MySystem.run {
            fastForEach {
                it.int
                it.double
            }
        }
    }

//    @Benchmark
//    fun emptyEntity() {
//        entity()
//    }

}
