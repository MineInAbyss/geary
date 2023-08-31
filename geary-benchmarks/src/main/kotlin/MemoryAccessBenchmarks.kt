package test

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.experimental.or
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

data class TestData(val int: Int, val double: Double)

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 0)
@Measurement(iterations = 0, time = 1, timeUnit = TimeUnit.SECONDS)

class MemoryAccessBenchmarks {
    private final val oneMil = 1000000
    var intArr = intArrayOf()
    var byteArr = byteArrayOf()
    var objArr = arrayOf<TestData>()
    var anyArr = mutableListOf<Any>()
    var accessorWithScope = AccessorWithScope(intArrayOf())

    var nDimensionalArr = Array(8) { IntArray(oneMil) { it } }

    @Setup
    fun setup() {
        intArr = IntArray(oneMil) { it }
        byteArr = ByteArray(oneMil) { it.toByte() }
        objArr = Array(oneMil) { TestData(it, it.toDouble()) }
        anyArr = MutableList(oneMil) { TestData(it, it.toDouble()) }
        accessorWithScope = AccessorWithScope(intArr)
    }

    // >2bil ops/s
//    @Benchmark
    fun emptyLoop() {
        for (i in 0 until oneMil) {
        }
    }

    // 1.8bil ops/s
    @Benchmark
    fun readIntArrDirectly() {
        for (i in 0 until oneMil) {
            intArr[i] + 1
        }
    }

    // 1.7bil ops/s
    @Benchmark
    fun readObjArrDirectly() {
        for (i in 0 until oneMil) {
            objArr[i]
        }
    }

    @Benchmark
    fun readObjArrWithAny() {
        for (i in 0 until oneMil) {
            anyArr[i]
        }
    }

    // 1.3bil ops/s
    @Benchmark
    fun read2ArrDirectly() {
        for (i in 0 until oneMil) {
            intArr[i] + 1
            byteArr[i] or 1
        }
    }

    @Benchmark
    fun read8ArrDirectly() {
        for (i in 0 until oneMil) {
            nDimensionalArr[0][i] + 1
            nDimensionalArr[1][i] + 1
            nDimensionalArr[2][i] + 1
            nDimensionalArr[3][i] + 1
            nDimensionalArr[4][i] + 1
            nDimensionalArr[5][i] + 1
            nDimensionalArr[6][i] + 1
            nDimensionalArr[7][i] + 1
        }
    }


    // 23470 ops/s
    @Benchmark
    fun readWriteSingleIntArrDirectly() {
        for (i in 0 until oneMil) {
            intArr[i] = intArr[i] + 1
        }
    }


    // 15503 ops/s
    @Benchmark
    fun readWriteTwoArrDirectly() {
        for (i in 0 until oneMil) {
            intArr[i] = intArr[i] + 1
            byteArr[i] = byteArr[i] or 1
        }
    }

    @Benchmark
    fun readObjPackToLocalArr() {
        for (i in 0 until oneMil) {
            val localArr = intArrayOf(intArr[i])
        }
    }

    @Benchmark
    fun readObjPackManyToLocalArr() {
        for (i in 0 until oneMil) {
            val localArr = arrayOf(intArr[i], byteArr[i], objArr[i])
        }
    }

    // 1907 ops/s
    @Benchmark
    fun readPackToSeparateArr() {
        val arr = IntArray(oneMil)
        for (i in 0 until oneMil) {
            arr[i] = intArr[i]
        }
    }

    // 1.5bil ops/s
    // Even with a full object instantiation, we don't lose much!
    @Benchmark
    fun readIntArrWithIndirection() {
        for (i in 0 until oneMil) {
            val acc by Accessor(i)
            acc + 1
        }
    }


    @Benchmark
    fun readIntArrWithIndirectionAndScope() {
        val accessors = object {
            val Int.acc by accessorWithScope
        }

        for (i in 0 until oneMil) {
            with(accessors) {
                with(i) {
                    acc + 1
                }
            }
        }
    }

    inner class Accessor(val index: Int) : ReadOnlyProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return intArr[index]
        }
    }

    @JvmInline
    value class AccessorWithScope(private val array: IntArray) : ReadOnlyProperty<Int, Int> {
        override fun getValue(thisRef: Int, property: KProperty<*>): Int {
            return array[thisRef]
        }
    }
}

