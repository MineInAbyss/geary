package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RemovableAccessorTest: GearyTest() {
    class MyQueryRemovable : Query() {
        var Pointer.data by get<Comp1>().removable()
    }

    @OptIn(UnsafeAccessors::class)
    @Test
    fun `should allow removing component via removable accessor`() {
        resetEngine()
        entity {
            set(Comp1(1))
        }
        var count = 0

        MyQueryRemovable().run {
            fastForEach {
                it.data shouldBe Comp1(1)
                it.data = null
                it.data shouldBe null
                it.entity.has<Comp1>() shouldBe false
                count++
            }
        }
        count shouldBe 1
    }
}
