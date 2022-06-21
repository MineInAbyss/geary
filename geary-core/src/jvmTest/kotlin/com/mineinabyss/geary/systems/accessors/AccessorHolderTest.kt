package com.mineinabyss.geary.systems.accessors

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.accessors.building.map
import com.mineinabyss.geary.systems.query.GearyQuery
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test

internal class AccessorHolderTest : GearyTest() {
    object FancyQuery : GearyQuery() {
        val TargetScope.default by getOrDefault<String>("empty!")
        val TargetScope.mapped by get<Int>().map { it.toString() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun fancyAccessors() {
        val entity = entity()
        FancyQuery.firstOrNull { it.entity == entity } shouldBe null
        entity.set(1)
        FancyQuery.run {
            first { it.entity == entity }.apply {
                default shouldBe "empty!"
                mapped shouldBe "1"
            }
        }
    }
}
