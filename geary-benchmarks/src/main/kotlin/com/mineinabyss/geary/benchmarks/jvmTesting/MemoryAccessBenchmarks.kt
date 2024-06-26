package com.mineinabyss.geary.benchmarks.jvmTesting

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import kotlin.experimental.or
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

data class TestData(val int: Int, val double: Double)

@State(Scope.Benchmark)
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

    @Benchmark
    fun emptyLoop() {
        for (i in 0 until oneMil) {
        }
    }

    @Benchmark
    fun readIntArrDirectly() {
        for (i in 0 until oneMil) {
            intArr[i] + 1
        }
    }

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

    @Benchmark
    fun readWriteSingleIntArrDirectly() {
        for (i in 0 until oneMil) {
            intArr[i] = intArr[i] + 1
        }
    }


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
            intArrayOf(intArr[i])
        }
    }

    @Benchmark
    fun readObjPackManyToLocalArr() {
        for (i in 0 until oneMil) {
            arrayOf(intArr[i], byteArr[i], objArr[i])
        }
    }

    @Benchmark
    fun readPackToSeparateArr() {
        val arr = IntArray(oneMil)
        for (i in 0 until oneMil) {
            arr[i] = intArr[i]
        }
    }

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

