package com.mineinabyss.geary.events

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.EventQuery
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleEventTest : GearyTest() {
    class MyEvent()

    var called = 0

    fun myListener() = geary.listener(object : EventQuery() {
        val data by target.get<Int>()
        val myEvent by event.get<MyEvent>()
    }) {
        called++
    }

    fun sourceOnlyListener() = geary.listener(object : EventQuery() {
        val data by source.get<Int>()
        val myEvent by event.get<MyEvent>()
    }) {
        called++
    }

    @Test
    fun `simple set listener`() {
        called = 0
        val listener = myListener()
        val entity = entity {
            set(1)
        }
        val event = entity {
            set(MyEvent())
        }

        called shouldBe 0
        entity.callEvent(event)
        called shouldBe 1
        entity.callEvent(entity())
        called shouldBe 1
        entity().callEvent(event)
        called shouldBe 1
    }

    @Test
    fun `source only simple set listener`() {
        called = 0
        val listener = sourceOnlyListener()

        val target = entity()
        val source = entity {
            set(1)
        }
        val event = entity {
            set(MyEvent())
        }

        called shouldBe 0
        target.callEvent(event, source = source)
        called shouldBe 1
    }
}
