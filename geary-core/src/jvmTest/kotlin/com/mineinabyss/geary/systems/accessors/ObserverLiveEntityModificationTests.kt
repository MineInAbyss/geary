package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ObserverLiveEntityModificationTests : GearyTest() {
    private fun registerQuery() = geary.cache(object : Query() {
        var data by get<Comp1>()
    })

    @Test
    fun `should allow data modify via accessor`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }

        var count = 0
        registerQuery().forEach {
            data shouldBe Comp1(1)
            data = Comp1(10)
            data shouldBe Comp1(10)
            count++
        }
        count shouldBe 1
    }

    @OptIn(UnsafeAccessors::class)
    @Test
    fun `should allow data modify when entity archetype changed by SET`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }
        var count = 0

        registerQuery().forEach {
            data shouldBe Comp1(1)
            data = Comp1(10)
            unsafeEntity.set("Other comp")
            unsafeEntity.add<Int>()
            data shouldBe Comp1(10)
            count++
        }
        count shouldBe 1
    }

    @OptIn(UnsafeAccessors::class)
    @Test
    fun `should allow data modify when entity archetype changed by REMOVE`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }
        var count = 0

        registerQuery().forEach {
            data shouldBe Comp1(1)
            unsafeEntity.remove<Comp1>()
            data = Comp1(10)
            data shouldBe Comp1(10)
            unsafeEntity.set("Other comp")
            data shouldBe Comp1(10)
            count++
        }
        count shouldBe 1
    }
}
