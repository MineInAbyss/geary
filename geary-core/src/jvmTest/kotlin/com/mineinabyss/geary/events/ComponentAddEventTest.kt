package com.mineinabyss.geary.events

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ComponentAddEventTest : GearyTest() {
    var inc = 0

    fun onStringAdd() = geary.listener(object : ListenerQuery() {
        val string by get<String>()
        val int by get<Int>()
        val double by get<Double>()
        override fun ensure() = event.anySet(::string, ::int, ::double)
    }).exec { inc++ }

    @Test
    fun componentAddEvent() {
        val listener = onStringAdd()

        entity {
            fun addedListeners() = type.getArchetype().targetListeners.count { it === listener }
            set("")
            set(1)
            inc shouldBe 0
            addedListeners() shouldBe 0
            set(1.0)
            addedListeners() shouldBe 1
            inc shouldBe 1

            set(1f)
            addedListeners() shouldBe 1
            inc shouldBe 1
            set("a")
            addedListeners() shouldBe 1
            inc shouldBe 2
        }
    }
}
