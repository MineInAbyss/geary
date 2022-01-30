package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class GearyEntityWithExtensionsKtTest : GearyTest() {
    @Test
    fun nullable_with_extensions() = runTest {
        val entity = entity {
            set("")
            set(1)
        }

        (entity.with { _: String, _: Int -> true } ?: false) shouldBe true
        (entity.with { _: String, _: Double -> true } ?: false) shouldBe false
        (entity.with { _: String, _: Double? -> true } ?: false) shouldBe true
    }
}
