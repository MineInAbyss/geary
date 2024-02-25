package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.Query
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FamilyMatchingTest : GearyTest() {
    val stringId = componentId<String>() or HOLDS_DATA
    val intId = componentId<Int>()

    val system = geary.system(object : Query() {
        val string by get<String>()
        override fun ensure() = this { has<Int>() }
    }).defer { string }.onFinish { data, entity ->
        data shouldBe entity.get<String>()
        entity.has<Int>() shouldBe true
    }

    val root = archetypes.archetypeProvider.rootArchetype
    val correctArchetype = root + stringId + intId

    @Test
    fun `family type is correct`() {
        // TODO families can are wrapped by accessors now, so components won't be directly on it
//        EntityType(system.family.components).getArchetype() shouldBe root + stringId
    }

    @Test
    fun `archetypes have been matched correctly`() {
        system.runner.matchedArchetypes shouldContain correctArchetype
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
        geary.queryManager.getEntitiesMatching(system.runner.family).shouldContainAll(entity, entity2)
    }

    @Test
    fun `accessors in system correctly read data`() {
        system.tick()
    }
}
