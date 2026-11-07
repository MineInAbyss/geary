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

    @Test
    fun indexOf() {
        val relation = ULong.MAX_VALUE - 5u
        val type = EntityType(listOf(relation, 10u, 3u, 7u))
        type.indexOf(3u) shouldBe 0
        type.indexOf(7u) shouldBe 1
        type.indexOf(relation) shouldBe 3
        type.indexOf(5u) shouldBe -1
        type.indexOf(ULong.MAX_VALUE) shouldBe -1
        EntityType().indexOf(1u) shouldBe -1
        (5u in type) shouldBe false
        (10u in type) shouldBe true
    }

    @Test
    fun plusTypeStaysSorted() {
        val combined = EntityType(listOf(5u, 1u)) + EntityType(listOf(3u, 1u))
        combined.inner shouldBe ulongArrayOf(1u, 3u, 5u)
        combined.indexOf(3u) shouldBe 1
    }
}
