package com.mineinabyss.geary.observers

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EntityGetAsFlowTest : GearyTest() {
    @Test
    fun `getAsFlow should correctly listen to entity updates`() = runTest {
        val entity = entity()
        val collected = mutableListOf<Int>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            entity.getAsFlow<Int>().collect {
                if(it != null) collected.add(it)
                else collected.add(0)
            }
        }
        entity.set(1)
        entity.set("other component")
        entity.set(2)
        entity.remove<Int>()
        entity.set(3)

        collected shouldContainExactly listOf(0, 1, 2, 0, 3)
    }

    @Test
    fun `getAsFlow should unregister itself when cancelled`()  = runTest {
        val entity = entity()
        val collected = mutableListOf<Int>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            entity.getAsFlow<Int>().collect {
                if(it != null) collected.add(it)
                else collected.add(0)
            }
            val collecting = launch(UnconfinedTestDispatcher(testScheduler)) {
                entity.getAsFlow<Int>().collect {
                    if(it != null) collected.add(it)
                    else collected.add(0)
                }
            }
            entity.set(1)
            collecting.cancel()
            entity.set(2)
            collected shouldContainExactly listOf(0, 1)
        }
    }
}
