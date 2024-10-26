package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class GearyEntityWithOperatorTest : GearyTest() {
    @Test
    fun nullable_with_extensions() {
        val entity = entity {
            set("")
            set(1)
        }

        (entity.with { _: String, _: Int -> true } ?: false) shouldBe true
        (entity.with { _: String, _: Double -> true } ?: false) shouldBe false
        (entity.with { _: String, _: Double? -> true } ?: false) shouldBe true
    }
}
