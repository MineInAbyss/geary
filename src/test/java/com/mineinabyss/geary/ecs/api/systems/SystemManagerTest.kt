package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.engine.*
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
internal class SystemManagerTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Nested
    inner class FamilyMatchingTest {
        val entity = Engine.entity {
            set("Test")
            add<Int>()
        }
        val entity2 = Engine.entity {
            set("Test")
            set(1)
        }

        val system = object : TickingSystem() {
            val string by get<String>()
            val int = has<Int>()

            override fun GearyEntity.tick() {
                string shouldBe get<String>()
                entity.has<Int>() shouldBe true
            }
        }
        val stringId = componentId<String>() or HOLDS_DATA
        val intId = componentId<Int>()

        val correctArchetype = root + stringId + intId

        init {
            SystemManager.registerSystem(system)
        }

        @Test
        fun `family type is correct`() {
            system.family.type.getArchetype() shouldBe correctArchetype
        }

        @Test
        fun `archetypes have been matched correctly`() {
            system.matchedArchetypes shouldContain correctArchetype
        }

        @Test
        fun `get entities matching family`() {
            SystemManager.getEntitiesMatching(system.family).apply {
                shouldContain(entity)
                shouldNotContain(entity2)
            }
        }

        @Test
        fun `accessors in system correctly read data`() {
            system.tick()
        }
    }

    @Nested
    inner class ConcurrentModificationTests {
        var ran = 0
        val removingSystem = object : TickingSystem() {
            val string by get<String>()

            override fun GearyEntity.tick() {
                remove<String>()
                getComponents() shouldBe setOf()
                ran++
            }
        }

        init {
            SystemManager.registerSystem(removingSystem)
        }

//        @Test
        fun `concurrent modification`() {
            val entities = (0 until 10).map { Engine.entity { set("Test") } }
            val total = SystemManager.getEntitiesMatching(Family(sortedSetOf(componentId<String>() or HOLDS_DATA))).count()
            removingSystem.tick()
            ran shouldBe total
            entities.map { it.getComponents() } shouldBe entities.map { setOf() }
        }
    }
}
