package com.mineinabyss.geary.events

import com.mineinabyss.geary.events.types.OnEntityRemoved
import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.observe
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SimpleComponentAddObserverTest : GearyTest() {
    var called = 0
    fun myListener() = geary.observe<OnSet>().involving<Int>().exec { called += 1 }

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
