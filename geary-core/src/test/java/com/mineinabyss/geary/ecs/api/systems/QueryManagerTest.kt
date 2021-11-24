package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.GearyAccessorScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.engine.getArchetype
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
internal class queryManagerTest {
    val engine: GearyEngine = GearyEngine()
    val queryManager = engine.queryManager

    @Nested
    inner class FamilyMatchingTest : GearyAccessorScope(engine) {
        val entity = engine.entity {
            it.set("Test")
            it.add<Int>()
        }
        val entity2 = engine.entity {
            it.set("Test")
            it.set(1)
        }

        val system = object : TickingSystem(engine) {
            val ResultScope.string by get<String>()
            val int = has<Int>()

            override fun ResultScope.tick() {
                string shouldBe entity.get<String>()
                entity.has<Int>() shouldBe true
            }
        }
        val stringId = componentId<String>() or HOLDS_DATA
        val intId = componentId<Int>()

        val correctArchetype = engine.root + stringId + intId

        init {
            queryManager.trackQuery(system)
        }

        @Test
        fun `family type is correct`() {
            GearyType(system.family.components).getArchetype(engine) shouldBe engine.root + stringId
        }

        @Test
        fun `archetypes have been matched correctly`() {
            system.matchedArchetypes shouldContain correctArchetype
        }

        @Test
        fun `get entities matching family`() {
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
    inner class ConcurrentModificationTests : GearyAccessorScope(engine) {
        var ran = 0

        val removingSystem = object : TickingSystem(engine) {
            val ResultScope.string by get<String>()

            override fun ResultScope.tick() {
                entity.remove<String>()
                ran++
            }
        }

        init {
            queryManager.trackQuery(removingSystem)
        }

        //FIXME who needs systems to work properly anyways
        @Test
        fun `concurrent modification`() {
            val entities = (0 until 10).map { engine.entity { it.set("Test") } }
            val total =
                queryManager.getEntitiesMatching(family(engine) {
                    hasData<String>()
                }).count()
            removingSystem.doTick()
            ran shouldBe total
            entities.map { it.getComponents() } shouldContainExactly entities.map { setOf() }
        }
    }

    @Nested
    inner class RelationshipTests : GearyAccessorScope(engine) {

        private inner class RelationTestComponent

        @Test
        fun relations() {
            var ran = 0
            val system = object : TickingSystem(engine) {
                val ResultScope.test by relation<RelationTestComponent>()
                override fun ResultScope.tick() {
                    ran++
                    family.relationDataTypes.map { it.id } shouldContain test.relation.id
                    test.parentData.shouldBeInstanceOf<RelationTestComponent>()
                }
            }
            system.family.relationDataTypes.shouldContainExactly(RelationDataType(componentId<RelationTestComponent>()))
            queryManager.trackQuery(system)
            val entity = engine.entity {
                it.setRelation<RelationTestComponent, String>(RelationTestComponent())
                it.add<String>()
            }
            val entity2 = engine.entity {
                it.setRelation<RelationTestComponent, Int>(RelationTestComponent())
                it.add<Int>()
            }
            val entity3 = engine.entity {
                it.setRelation<String, RelationTestComponent>("")
                it.add<RelationTestComponent>()
            }
            family(engine) { has(entity.type) }.relationDataTypes.first() shouldBe system.family.relationDataTypes.first()
            system.matchedArchetypes.shouldContainAll(
                entity.type.getArchetype(engine),
                entity2.type.getArchetype(engine)
            )
            system.matchedArchetypes.shouldNotContain(entity3.type.getArchetype(engine))

            system.doTick()
            ran shouldBe 2

        }

        private inner class RelationTestComponent1
        private inner class RelationTestComponent2

        @Test
        fun relationPermutations() {
            var ran = 0
            val system = object : TickingSystem(engine) {
                val ResultScope.test1 by relation<RelationTestComponent1>()
                val ResultScope.test2 by relation<RelationTestComponent2>()
                override fun ResultScope.tick() {
                    ran++
                    test1.parentData.shouldBeInstanceOf<RelationTestComponent1>()
                    test2.parentData.shouldBeInstanceOf<RelationTestComponent2>()
                }
            }
            queryManager.trackQuery(system)

            engine.entity {
                it.setRelation<RelationTestComponent1, String>(RelationTestComponent1())
                it.setRelation<RelationTestComponent1, Int>(RelationTestComponent1())
                it.setRelation<RelationTestComponent2, String>(RelationTestComponent2())
                it.setRelation<RelationTestComponent2, Int>(RelationTestComponent2())
                it.add<String>()
            }

            system.doTick()

            ran shouldBe 4
        }

        inner class RelationTestWithData

        @Test
        fun relationsWithData() {
            val system = object : TickingSystem(engine) {
                val ResultScope.withData by relationWithData<RelationTestWithData>()

                override fun ResultScope.tick() {
                    withData.parentData.shouldBeInstanceOf<RelationTestWithData>()
                    withData.componentData shouldBe "Test"
                }
            }

            val entity = engine.entity {
                it.setRelation<RelationTestWithData, String>(RelationTestWithData())
                it.add<String>()
            }

            val entityWithData = engine.entity {
                it.setRelation<RelationTestWithData, String>(RelationTestWithData())
                it.set("Test")
            }

            queryManager.trackQuery(system)

            system.matchedArchetypes.shouldNotContain(entity.type.getArchetype(engine))
            system.matchedArchetypes.shouldContain(entityWithData.type.getArchetype(engine))

            system.doTick()
        }
    }
}
