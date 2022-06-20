package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.GearyType
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FamilyMatchingTest: GearyTest() {
    val stringId = componentId<String>() or HOLDS_DATA
    val intId = componentId<Int>()

    val system = object : TickingSystem() {
        val TargetScope.string by get<String>()
        val TargetScope.int by family { has<Int>() }

        override fun TargetScope.tick() {
            string shouldBe entity.get<String>()
            entity.has<Int>() shouldBe true
        }
    }

    val correctArchetype = engine.rootArchetype + stringId + intId

    init {
        queryManager.trackQuery(system)
    }

    @Test
    fun `family type is correct`() {
        GearyType(system.family.components).getArchetype() shouldBe engine.rootArchetype + stringId
    }

    @Test
    fun `archetypes have been matched correctly`() {
        system.matchedArchetypes shouldContain correctArchetype
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
        queryManager.getEntitiesMatching(system.family).shouldContainAll(entity, entity2)
    }

    @Test
    fun `accessors in system correctly read data`() {
        system.doTick()
    }
}
