package com.mineinabyss.geary.events

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ComponentAddEventKtTest : GearyTest() {
    var inc = 0

    //TODO write test for all methods of checking for added
    inner class OnStringAdd : GearyListener() {
        // All three get added
        val TargetScope.string by added<String>()
        val TargetScope.int by added<Int>()
        val TargetScope.double by added<Double>()

        @Handler
        fun increment() {
            inc++
        }
    }

    @Test
    fun componentAddEvent() {
        val listener = OnStringAdd()
        engine.addSystem(listener)

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
            set("")
            addedListeners() shouldBe 1
            //TODO decide on this behaviour
            inc shouldBe 1
        }
    }
}
