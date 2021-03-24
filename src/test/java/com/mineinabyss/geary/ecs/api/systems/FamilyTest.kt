@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.mineinabyss.geary.ecs.api.systems

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class FamilyTest {
    @Test
    fun contains() {
        val family = family {
            match = sortedSetOf(1uL, 2uL, 3uL)
        }
        (sortedSetOf(1uL, 2uL) in family) shouldBe false
        (sortedSetOf(1uL, 2uL, 3uL) in family) shouldBe true
        (sortedSetOf(1uL, 2uL, 3uL, 4uL) in family) shouldBe true
    }

    @Test
    fun `contains trait`() {
        val family = Family(traits = sortedSetOf(traitFor(15uL)))

        (sortedSetOf(traitFor(14uL, 1uL), 1uL) in family) shouldBe false
        (sortedSetOf(traitFor(15uL, 1uL)) in family) shouldBe false
        (sortedSetOf(traitFor(15uL, 1uL), 1uL) in family) shouldBe true
    }
}
