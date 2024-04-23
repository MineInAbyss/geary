package com.mineinabyss.geary.events

import com.mineinabyss.geary.events.types.OnRemove
import com.mineinabyss.geary.events.types.OnSet
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.builders.observe
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ComponentAddEventTest : GearyTest() {
    @Test
    fun componentAddEvent() {
        var inc = 0

        geary.observe<OnSet>()
            .involving<String, Int, Double>()
            .exec { inc++ }

        entity {
            set("")
            set(1)
            inc shouldBe 0
            set(1.0)
            inc shouldBe 1
            set(1f)
            inc shouldBe 1
            set("a")
            inc shouldBe 2
        }
    }
}
