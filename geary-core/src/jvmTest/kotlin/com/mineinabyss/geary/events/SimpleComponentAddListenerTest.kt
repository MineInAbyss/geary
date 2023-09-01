package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleComponentAddListenerTest : GearyTest() {
    class MyListener : Listener() {
        var called = 0

        val Records.data by onSet<Int>()

        override fun Records.handle() {
            called += 1
        }
    }

    @Test
    fun `simple query`() {
        val listener = MyListener()
        geary.pipeline.addSystem(listener)

        val entity = entity()
        listener.called shouldBe 0
        entity.set(1.0)
        listener.called shouldBe 0
        entity.set(1)
        listener.called shouldBe 1
    }
}
