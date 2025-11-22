package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SystemFamilyMatchingTest : GearyTest() {
    val stringId = componentId<String>() or HOLDS_DATA
    val intId = componentId<Int>()

    val system = system(object : Query(this) {
        val string by get<String>()
        override fun ensure() = this { has<Int>() }
    }).defer { it.string }.onFinish { data, entity ->
        data shouldBe entity.get<String>()
        entity.has<Int>() shouldBe true
    }

    val root = rootArchetype
    val correctArchetype = root + stringId + intId

    @Test
    fun `archetypes have been matched correctly`() {
        system.runner.matchedArchetypes.asList() shouldContain correctArchetype
    }

    @Test
    fun `get entities matching family`() {
        val entity = entity {
            set("Test")
            add<Int>()
        }
        val entity2 = entity {
            set("Test")
            set(1)
        }
        val entities = findEntities(system.runner.family)
        entities.shouldContainAll(entity, entity2)
    }

    @Test
    fun `accessors in system correctly read data`() {
        system.tick()
    }
}
