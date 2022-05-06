package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.helpers.GearyTest
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.accessors.building.map
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.geary.systems.query.invoke
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
        //TODO put back when Koin comes
//        FancyQuery.toList().isEmpty() shouldBe true
        FancyQuery.firstOrNull { it.entity == entity } shouldBe null
        entity.set(1)
        FancyQuery {
            first { it.entity == entity }.apply {
                default shouldBe "empty!"
                mapped shouldBe "1"
            }
        }
    }
}
