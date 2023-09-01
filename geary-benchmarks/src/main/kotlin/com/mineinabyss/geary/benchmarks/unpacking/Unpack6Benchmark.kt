package com.mineinabyss.geary.benchmarks.unpacking

import com.mineinabyss.geary.benchmarks.helpers.*
import com.mineinabyss.geary.datatypes.GearyRecord
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.GearyQuery
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class Unpack6Benchmark {
    object SystemOf6 : GearyQuery() {
        val GearyRecord.comp1 by get<Comp1>()
        val GearyRecord.comp2 by get<Comp2>()
        val GearyRecord.comp3 by get<Comp3>()
        val GearyRecord.comp4 by get<Comp4>()
        val GearyRecord.comp5 by get<Comp5>()
        val GearyRecord.comp6 by get<Comp6>()

    }

    object SystemOf1 : GearyQuery() {
        val GearyRecord.comp1 by get<Comp1>()
    }

    @Setup
    fun setUp() {
        geary(TestEngineModule) {
        }

        repeat(tenMil) {
            entity {
                set(Comp1(0))
                set(Comp2(0))
                set(Comp3(0))
                set(Comp4(0))
                set(Comp5(0))
                set(Comp6(0))
            }
        }
    }

    // 4.890 ops/s
    @Benchmark
    fun unpack1of6Comp() {
        SystemOf1.run {
            fastForEach {
                it.comp1
            }
        }
    }

    // 1.749 ops/s
    @Benchmark
    fun unpack6of6Comp() {
        SystemOf6.run {
            fastForEach {
                it.comp1
                it.comp2
                it.comp3
                it.comp4
                it.comp5
                it.comp6
            }
        }
    }
}
