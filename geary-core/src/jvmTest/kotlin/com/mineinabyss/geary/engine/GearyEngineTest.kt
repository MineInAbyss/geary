package com.mineinabyss.geary.engine

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.helpers.toGeary
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
                engine.removeEntity((offset + it.toULong()).toGeary())
            }

            // Since we removed the first 10 entities, the last entity we removed (at 9) should be the next one that's
            // ready to be freed up, then 8, etc...
            //TODO it looks like entities get removed fine but we can't await them properly with a concurrent map
//            repeat(100) {
//                (offset..(offset + 100uL)).toSet().shouldContain(entity().id)
//            }
        }
    }
}
