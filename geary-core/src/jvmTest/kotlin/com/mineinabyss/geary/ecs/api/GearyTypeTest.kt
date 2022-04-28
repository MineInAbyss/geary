package com.mineinabyss.geary.ecs.api

import com.mineinabyss.geary.datatypes.GearyType
import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class GearyTypeTest : GearyTest() {
    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun typeSorting() {
        val type = GearyType(listOf(1u, 2u, 3u))
        type.inner shouldBe ulongArrayOf(1u, 2u, 3u)
        (type + 4u).inner shouldBe ulongArrayOf(1u, 2u, 3u, 4u)
        (type + 1u).inner shouldBe ulongArrayOf(1u, 2u, 3u)
    }
}
