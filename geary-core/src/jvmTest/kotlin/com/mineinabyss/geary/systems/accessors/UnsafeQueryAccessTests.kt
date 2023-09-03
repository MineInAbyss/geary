package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UnsafeQueryAccessTests : GearyTest() {
    class MyQuery : Query() {
        var Pointer.data by get<Comp1>()
    }


    @Test
    fun `should allow data modify via accessor`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }

        var count = 0
        MyQuery().run {
            fastForEach {
                it.data shouldBe Comp1(1)
                it.data = Comp1(10)
                it.data shouldBe Comp1(10)
                count++
            }
        }
        count shouldBe 1
    }

    @OptIn(UnsafeAccessors::class)
    @Test
    fun `should allow data modify when entity changed archetype by setting`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }

        var count = 0
        MyQuery().run {
            fastForEach {
                it.data shouldBe Comp1(1)
                it.data = Comp1(10)
                it.entity.set("Other comp")
                it.entity.add<Int>()
                it.data shouldBe Comp1(10)
                count++
            }
        }
        count shouldBe 1
    }

    @OptIn(UnsafeAccessors::class)
    @Test
    fun `should allow data modify when entity changed archetype by removing`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }

        var count = 0
        MyQuery().run {
            fastForEach {
                it.data shouldBe Comp1(1)
                it.entity.remove<Comp1>()
                it.data = Comp1(10)
                it.data shouldBe Comp1(10)
                it.entity.set("Other comp")
                it.data shouldBe Comp1(10)
                count++
            }
        }
        count shouldBe 1
    }

}
