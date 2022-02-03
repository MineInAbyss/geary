@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package com.mineinabyss.geary.ecs.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


suspend fun main() = coroutineScope {
//    limitedParallelism()
    println(measureTime {
        locksWithLimited()
//        limitedParallelism()
    })
//    val result = runConcurrencyTests().joinToString(separator = "\n")
//    println("Result:\n$result")
}

suspend fun locksBlock() {
    var inc = 0
    val a = Mutex()
    val b = Mutex()
    withContext(Dispatchers.IO) {
        repeat(1000) {
            launch {
                a.withLock {
                    b.withLock {
                        inc++
                    }
                }
            }
            launch {
                b.withLock {
                    a.withLock {
                        inc++
                    }
                }
            }
        }
    }
    println(inc)
}

suspend fun limitedParallelism() {
    var inc = 0
    val a = Dispatchers.Default.limitedParallelism(1)
    val b = Dispatchers.Default.limitedParallelism(1)
    withContext(Dispatchers.IO) {
        repeat(1000) {
            launch {
                withContext(a) {
                    withContext(b) {
                        inc++
                    }
                }
            }
            launch {
                withContext(b) {
                    withContext(a) {
                        inc++
                    }
                }
            }
        }
    }
    println(inc)
}

suspend fun locksWithLimited() {
    var inc = 0
    val a = Mutex()
    val b = Mutex()
    val limit = Dispatchers.Default.limitedParallelism(1)
    withContext(Dispatchers.IO) {
        a.lock()
        launch {
            delay(1000)
            a.unlock()
        }
        repeat(1) {
            launch {
                withContext(limit) {
                    println("Locking A1")
                    a.withLock {
                        println("Locking B1")
                        b.withLock {
                            inc++
                        }
                        println("Unlocked B1")
                    }
                    println("Unlocked A1")
                }
            }
            launch {
                withContext(limit) {
                    println("Locking B2")
                    b.withLock {
                        println("Locking A2")
                        a.withLock {
                            inc++
                        }
                        println("Unlocked A2")
                    }
                    println("Unlocked B2")
                }
            }
        }
    }
    println(inc)
}

//Result:
//Base: counter: 237933, duration: 90.672898ms
//SyncBlock: counter: 1000000, duration: 21.554582ms
//Mutex: counter: 1000000, duration: 67.764557ms
//Atomic: counter: 1000000, duration: 32.365875ms
//LimitedDispatcher: 1000000, duration: 17.180316ms
//SingleDispatcher: 1000000, duration: 39.506109ms


suspend fun runConcurrencyTests(): List<String> = coroutineScope {
    buildList {
        this += runBase()
        this += runSyncBlock()
        this += runMutex()
        this += runAtomic()
        this += runLimitedDispatcher()
        this += runSingleDispatcher()
    }
}

suspend fun runBase(): String {
    var counter: Int = 0
    val duration = measureTime {
        withContext(Dispatchers.Default) {
            repeat(1000) {
                launch {
                    repeat(1000) { ++counter } // 62ms, with the incorrect result value.
                }
            }
        }
    }
    val message = "Base: $counter, duration: $duration" // ~647245, but should be 1000_000
    return message
}

suspend fun runSyncBlock(): String {
    val lock = intArrayOf()
    var counter: Int = 0
    val duration = measureTime {
        withContext(Dispatchers.Default) {
            repeat(1000) {
                launch {
                    synchronized(lock) { // 55 ms
                        repeat(1000) { ++counter }
                    }
                }
            }
        }
    }
    val message = "SyncBlock: $counter, duration: $duration"
    return message
}

suspend fun runMutex(): String {
    var counter: Int = 0
    val mutex = Mutex()
    val duration = measureTime {
        withContext(Dispatchers.Default) {
            repeat(1000) {
                launch {
                    mutex.withLock { // 100ms
                        repeat(1000) { ++counter }
                    }
                }
            }
        }
    }
    val message = "Mutex: $counter, duration: $duration"
    return message
}

suspend fun runAtomic(): String {
    val atomicCounter = AtomicInteger(0)
    val duration = measureTime {
        withContext(Dispatchers.Default) {
            repeat(1000) {
                launch {
                    repeat(1000) { atomicCounter.incrementAndGet() } // 60ms
                }
            }
        }
    }
    val message = "Atomic: $atomicCounter, duration: $duration"
    return message
}

suspend fun runLimitedDispatcher(): String {
    var counter: Int = 0
    val limitedDispatcher = Dispatchers.Default.limitedParallelism(1)
    val duration = measureTime {
        withContext(Dispatchers.Default) {
            repeat(1000) {
                launch {
                    withContext(limitedDispatcher) { // 90ms
                        repeat(1000) { ++counter }
                    }
                }
            }
        }
    }
    val message = "LimitedDispatcher: $counter, duration: $duration"
    return message
}

suspend fun runSingleDispatcher(): String {
    var counter: Int = 0
    val singleDispatcher = newSingleThreadContext("single")
    val duration = measureTime {
        withContext(Dispatchers.Default) {
            repeat(1000) {
                launch {
                    withContext(singleDispatcher) { // 90ms
                        repeat(1000) { ++counter }
                    }
                }
            }
        }
    }
    val message = "SingleDispatcher: $counter, duration: $duration"
    return message
}
