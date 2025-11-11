package com.mineinabyss.geary.observers

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RemoveObserverTest : GearyTest() {
    @Test
    fun `should stop observing events after removed`() {
        // arrange
        val called = mutableListOf<Int>()
        val observer = observe<OnSet>().exec(query<Int>()) { (data) ->
            called += data
        }

        // act
        val entity = entity {
            set(1)
            set(2)
        }
        observer.close()
        entity.set(3)

        // assert

        called shouldBe listOf(1, 2)
    }
}
