package com.mineinabyss.geary.engine

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.geary
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GearyEngineTest : GearyTest() {
    @Nested
    inner class EntityRemoval {
        @Test
        fun `entity removal and reuse`() {
            val offset = entity().id + 1uL
            repeat(100) {
                entity()
            }

            // We filled up ids 0..9, so next should be at 10
            entity().id shouldBe offset + 100uL

            (0 until 100).forEach {
                geary.entityProvider.remove((offset + it.toULong()).toGeary())
            }
        }
    }
}
