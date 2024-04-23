package com.mineinabyss.geary.events

import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleEventTest : GearyTest() {
    sealed class MyEvent

    var called = 0

    fun myListener() = geary.observe<MyEvent>().filter(Query.of<Int>()).exec { called++ }

    @Test
    fun `simple set listener`() {
        called = 0
        val listener = myListener()
        val entity = entity {
            set(1)
        }
        called shouldBe 0
        entity.emit<MyEvent>()
        called shouldBe 1
        entity.emit<String>()
        called shouldBe 1
        entity().emit<MyEvent>()
        called shouldBe 1
    }
}
