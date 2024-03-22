package com.mineinabyss.geary.events

import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EntityRemoveListenerTest : GearyTest() {
    @Test
    fun `should correctly run multiple listeners on single event`() {
        var called = 0

        val listener1 = geary.listener(object : ListenerQuery() {
            val data by get<Int>()
            override fun ensure() = event { has<EntityRemoved>() }
        }).exec {
            data shouldBe 1
            entity.remove<Int>()
            called++
        }

        val listener2 = geary.listener(object : ListenerQuery() {
            val data by get<String>()
            override fun ensure() = event { has<EntityRemoved>() }
        }).exec {
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
