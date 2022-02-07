package com.mineinabyss.geary.ecs.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.koin.core.component.getScopeId

val dispatch = Dispatchers.IO + CoroutineName("Test")
private val test: Channel<Int> = Channel()
suspend fun main(): Unit = coroutineScope {
        launch {
            repeat(10) {
                test.send(it)
                delay(100)
            }
        }
        launch {
            delay(100)
            test.getScopeId()
            for(it in test) {
                println(it)
            }
        }
//    println(measureTime {
//        (0..10000).map {
//            launch(Dispatchers.Default) {
//                println("Launched!")
//                val dispatch = Dispatchers.IO.limitedParallelism(1)
////                repeat(5) {
//                    runBlocking(dispatch) {
////                        launch(dispatch) {
//                            delay(500)
////                        }
////
////                    }
//                }
//            }
//        }.joinAll()
//    })
////    repeat(10) {
////        launch {
////            doThing()
////            println("After launch!")
////        }
////    }
}

fun doThing() {
    runBlocking(dispatch) {
        delay(1000)
        val threadName = Thread.currentThread().name
        println("Running on thread: $threadName")
    }
}
