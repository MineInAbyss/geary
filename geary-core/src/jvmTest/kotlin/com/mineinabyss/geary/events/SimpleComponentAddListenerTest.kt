package com.mineinabyss.geary.events

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleComponentAddListenerTest : GearyTest() {
    var called = 0
    fun myListener() = geary.listener(object : ListenerQuery() {
        val data by get<Int>()
        override fun ensure() = event.anySet(::data)
    }).exec { called += 1 }

    @Test
    fun `simple event listener`() {
        myListener()

        val entity = entity()
        called shouldBe 0
        entity.set(1.0)
        called shouldBe 0
        entity.set(1)
        called shouldBe 1
    }
}
