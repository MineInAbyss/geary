package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Pointers
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleEventTest : GearyTest() {
    class MyEvent()

    class MyListener : Listener() {
        var called = 0

        val Records.data by get<Int>().on(target)
        val Records.event by get<MyEvent>().on(event)

        override fun Pointers.handle() {
            called += 1
        }
    }

    @Test
    fun `simple set listener`() {
        val listener = MyListener()
        geary.pipeline.addSystem(listener)

        val entity = entity {
            set(1)
        }
        val event = entity {
            set(MyEvent())
        }

        listener.called shouldBe 0
        entity.callEvent(event)
        listener.called shouldBe 1
        entity.callEvent(entity())
        listener.called shouldBe 1
        entity().callEvent(event)
        listener.called shouldBe 1
    }
}
