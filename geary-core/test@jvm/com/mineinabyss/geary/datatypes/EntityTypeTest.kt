package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class EntityTypeTest : GearyTest() {
    @Test
    fun typeSorting() {
        val type = EntityType(listOf(3u, 1u, 2u))
        type.inner shouldBe ulongArrayOf(1u, 2u, 3u)
        (type + 4u).inner shouldBe ulongArrayOf(1u, 2u, 3u, 4u)
        (type + 1u).inner shouldBe ulongArrayOf(1u, 2u, 3u)
    }
}
