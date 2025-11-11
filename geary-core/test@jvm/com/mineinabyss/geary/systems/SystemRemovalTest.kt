package com.mineinabyss.geary.systems

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.test.GearyTest
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SystemRemovalTest : GearyTest() {
    @Test
    fun `should stop tracking query after closed`() {
        // arrange
        resetEngine()
        val query = cache(query<Int>())

        // act
        entity { set(1) }
        val matchedArchetypesBeforeClose = query.matchedArchetypes.size
        query.close()
        entity { set(1); set("string") }
        val matchedArchetypesAfterClose = query.matchedArchetypes.size

        matchedArchetypesBeforeClose shouldBe 1
        matchedArchetypesAfterClose shouldBe 1
        shouldThrowAny {
            query.entities()
        }
    }

    @Test
    fun `should stop ticking systems after closed`() {
        // arrange
        resetEngine()
        val calls = mutableListOf<Int>()

        val system = system(query<Int>()).exec { (num) ->
            calls += num
        }
        entity { set(1) }

        // act
        tick()
        system.close()
        tick()

        // assert
        calls shouldBe listOf(1)
    }
}