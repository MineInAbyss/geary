package com.mineinabyss.geary.ecs.accessors

import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.accessors.building.getOrDefault
import com.mineinabyss.geary.ecs.accessors.building.map
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.query.Query
import com.mineinabyss.geary.ecs.query.invoke
import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class AccessorHolderTest : GearyTest() {

    object FancyQuery : Query() {
        val TargetScope.default by getOrDefault<String>("empty!")
        val TargetScope.mapped by get<Int>().map { it.toString() }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun fancyAccessors() = runTest {
        val entity = entity()
        //TODO put back when Koin comes
//        FancyQuery.toList().isEmpty() shouldBe true
        FancyQuery.flow().firstOrNull { it.entity == entity } shouldBe null
        entity.set(1)
        FancyQuery {
            flow().first { it.entity == entity }.apply {
                default shouldBe "empty!"
                mapped shouldBe "1"
            }
        }
    }
}
