package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.cachedQuery
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ListenerLiveEntityModificationTests : GearyTest() {
    private fun registerQuery() = geary.cachedQuery(object : Query() {
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

    @Test
    fun `should allow data modify when entity archetype changed by SET`() {
        resetEngine()
        var count = 0

        geary.listener(object : ListenerQuery() {
            var data by get<Comp1>()
        }).exec {
            data shouldBe Comp1(1)
            data = Comp1(10)
            entity.set("Other comp")
            entity.add<Int>()
            data shouldBe Comp1(10)
            count++
        }

        entity {
            set(Comp1(1))
        }.callEvent()
        count shouldBe 1
    }

    @Test
    fun `testing`() {
        entity {
            set(Comp1(1))
            remove<Comp1>()
            set(Comp1(10))
        }

    }

    @Test
    fun `should allow data modify when entity archetype changed by REMOVE`() {
        resetEngine()
        var count = 0

        geary.listener(object : ListenerQuery() {
            var data by get<Comp1>()
        }).exec {
            data shouldBe Comp1(1)
            entity.remove<Comp1>()
            data = Comp1(10)
            data shouldBe Comp1(10)
            entity.set("Other comp")
            data shouldBe Comp1(10)
            count++
        }
        entity {
            set(Comp1(1))
        }.callEvent()
        count shouldBe 1
    }
}
