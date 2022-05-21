package com.mineinabyss.geary.async

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.TickingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class RunSafelyTest: GearyTest() {
    class CheckAsyncSystem : TickingSystem() {
        val TargetScope.string by get<String>()
        override fun TargetScope.tick() {
            error("Found entity with string when it should have been removed before iteration")
        }
    }

    //TODO figure out what's up here
//    @Test
    fun runSafely() = runTest {
        clearEngine()
        engine.addSystem(CheckAsyncSystem())
        launch {
            repeat(5000) {
                engine.tick(it.toLong())
            }
        }
        concurrentOperation(50000) {
            com.mineinabyss.geary.helpers.runSafely {
                entity {
                    set("Hello world")
                }.removeEntity()
            }.await()
        }.awaitAll()
    }
}
