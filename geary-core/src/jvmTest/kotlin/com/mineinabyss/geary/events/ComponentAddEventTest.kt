package com.mineinabyss.geary.events

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.Records
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ComponentAddEventTest : GearyTest() {
    var inc = 0

    //TODO write test for all methods of checking for added
    inner class OnStringAdd : Listener() {
        // All three get added
        val Records.string by onSet<String>()
        val Records.int by onSet<Int>()
        val Records.double by onSet<Double>()

        override fun Records.handle() {
            inc++
        }
    }

    @Test
    fun componentAddEvent() {
        val listener = OnStringAdd()
        geary.pipeline.addSystem(listener)

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
