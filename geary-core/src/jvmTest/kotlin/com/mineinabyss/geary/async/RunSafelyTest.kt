package com.mineinabyss.geary.async

import com.mineinabyss.geary.datatypes.GearyRecord
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.RepeatingSystem


class RunSafelyTest : GearyTest() {
    class CheckAsyncSystem : RepeatingSystem() {
        val GearyRecord.string by get<String>()
        override fun GearyRecord.tick() {
            error("Found entity with string when it should have been removed before iteration")
        }
    }

    //TODO figure out what's up here
//    @Test
//    fun runSafely() = runTest {
//        clearEngine()
//        geary.systems.add(CheckAsyncSystem())
//        launch {
//            repeat(5000) {
//                geary.engine.tick(it.toLong())
//            }
//        }
//        concurrentOperation(50000) {
//            runSafely {
//                entity {
//                    set("Hello world")
//                }.removeEntity()
//            }.await()
//        }.awaitAll()
//    }
}
