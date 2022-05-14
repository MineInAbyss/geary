package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.GearyType
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.RelationValueId
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.TickingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class QueryManagerTest : GearyTest() {
    @Nested
    inner class FamilyMatchingTest {
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
            queryManager.getEntitiesMatching(system.family).apply {
                shouldContainAll(entity, entity2)
            }
        }

        @Test
        fun `accessors in system correctly read data`() {
            system.doTick()
        }
    }

    @Nested
    inner class ConcurrentModificationTests {
        var ran = 0

        val removingSystem = object : TickingSystem() {
            val TargetScope.string by get<String>()

            override fun TargetScope.tick() {
                entity.remove<String>()
                ran++
            }
        }

        init {
            queryManager.trackQuery(removingSystem)
        }

        @Test
        fun `concurrent modification`() {
            val entities = (0 until 10).map { entity { set("Test") } }
            val total =
                queryManager.getEntitiesMatching(family {
                    hasSet<String>()
                }).count()
            removingSystem.doTick()
            ran shouldBe total
            entities.map { it.getComponents() } shouldContainExactly entities.map { setOf() }
        }
    }

    private class RelationTestComponent

    @Test
    fun relations() {
        clearEngine()
        var ran = 0
        val system = object : TickingSystem() {
            val TargetScope.test by relation<Any?, RelationTestComponent>()
            override fun TargetScope.tick() {
                ran++
                family.relationTargetIds.map { it.id } shouldContain test.targetEntity
                test.target.shouldBeInstanceOf<RelationTestComponent>()
            }
        }
        queryManager.trackQuery(system)
        system.family.relationTargetIds.shouldContainExactly(RelationValueId(componentId<RelationTestComponent>()))
        val entity = entity {
            setRelation(String::class, RelationTestComponent())
            add<String>()
        }
        val entity2 = entity {
            setRelation(Int::class, RelationTestComponent())
            add<Int>()
        }
        val entity3 = entity {
            setRelation(RelationTestComponent::class, "")
            add<RelationTestComponent>()
        }
        system.matchedArchetypes.shouldContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        system.matchedArchetypes.shouldNotContain(entity3.type.getArchetype())

        engine.cleanup()
        system.doTick()
        ran shouldBe 2

    }

    private class RelationTestComponent1
    private class RelationTestComponent2

    @Test
    fun relationPermutations() {
        var ran = 0
        val system = object : TickingSystem() {
            val TargetScope.test1 by relation<Any?, RelationTestComponent1>()
            val TargetScope.test2 by relation<Any?, RelationTestComponent2>()
            override fun TargetScope.tick() {
                ran++
                test1.target.shouldBeInstanceOf<RelationTestComponent1>()
                test2.target.shouldBeInstanceOf<RelationTestComponent2>()
            }
        }
        queryManager.trackQuery(system)

        entity {
            setRelation(String::class, RelationTestComponent1())
            setRelation(Int::class, RelationTestComponent1())
            setRelation(String::class, RelationTestComponent2())
            setRelation(Int::class, RelationTestComponent2())
            add<String>()
        }

        engine.cleanup()
        system.doTick()

        ran shouldBe 4
    }

    class RelationTestWithData

    @Test
    fun relationsWithData() {
        val system = object : TickingSystem() {
            val TargetScope.withData by relation<Any, RelationTestWithData>()

            override fun TargetScope.tick() {
                withData.target.shouldBeInstanceOf<RelationTestWithData>()
                withData.type shouldBe "Test"
            }
        }

        val entity = entity {
            setRelation(String::class, RelationTestWithData())
            add<String>()
        }

        val entityWithData = entity {
            setRelation(String::class, RelationTestWithData())
            set("Test")
        }

        queryManager.trackQuery(system)

        system.matchedArchetypes.shouldNotContain(entity.type.getArchetype())
        system.matchedArchetypes.shouldContain(entityWithData.type.getArchetype())

        engine.cleanup()
        system.doTick()
    }

    private class TestComponent
    private object EventListener : GearyListener() {
        var ran = 0
        private val TargetScope.testComponent by get<TestComponent>()

        @Handler
        fun handle() {
            this@EventListener.ran++
        }
    }

    @Test
    fun `empty event handler`() {
        engine.addSystem(EventListener)
        (engine.rootArchetype.type in EventListener.event.family) shouldBe true
        engine.rootArchetype.eventHandlers.map { it.parentListener } shouldContain EventListener
        entity {
            set(TestComponent())
        }.callEvent()
        // 1 from setting, 1 from calling empty event
        EventListener.ran shouldBe 2
    }
}
