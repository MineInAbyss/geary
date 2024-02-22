package com.mineinabyss.geary.events

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.checkingListener
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CheckingListenerTest : GearyTest() {
    class MyEvent()

    fun myListener() = geary.listener(object: ListenerQuery() {
        val data by target.get<Int>()
    }).check { data > 10 }

    @Test
    fun `simple set listener`() {
        myListener()

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
