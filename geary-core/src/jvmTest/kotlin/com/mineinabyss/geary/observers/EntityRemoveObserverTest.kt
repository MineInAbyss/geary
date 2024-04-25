package com.mineinabyss.geary.observers

import com.mineinabyss.geary.events.types.OnEntityRemoved
import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.systems.query.query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EntityRemoveObserverTest : GearyTest() {
    @Test
    fun `should correctly run multiple listeners on single event`() {
        var called = 0

        val listener1 = geary.observe<OnEntityRemoved>().exec(query<Int>()) { (data) ->
            data shouldBe 1
            entity.remove<Int>()
            called++
        }

        val listener2 = geary.observe<OnEntityRemoved>().exec(query<String>()) { (data) ->
            data shouldBe ""
        }

        val entity1 = entity {
            set(1)
            set("")
        }

        val entity2 = entity {
            set(1)
            set("")
        }

        entity2.removeEntity()
        entity1.removeEntity()
        called shouldBe 2
    }
}
