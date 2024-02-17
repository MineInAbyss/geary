package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CheckingListenerTest : GearyTest() {
    class MyEvent()

    class MyListener : CheckingListener() {
        var called = 0

        val Records.data by get<Int>().on(target)

        override fun Pointers.check(): Boolean {
            return data > 10
        }
    }

    @Test
    fun `simple set listener`() {
        val listener = MyListener()
        geary.pipeline.addSystem(listener)

        val entityFail = entity {
            set(1)
        }
        val entityPass = entity {
            set(20)
        }

        entityFail.callCheck() shouldBe false
        entityPass.callCheck() shouldBe true
    }
}
