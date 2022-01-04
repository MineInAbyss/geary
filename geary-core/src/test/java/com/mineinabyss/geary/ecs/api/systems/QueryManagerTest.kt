package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.accessors.get
import com.mineinabyss.geary.ecs.accessors.relation
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import com.mineinabyss.geary.ecs.engine.*
import com.mineinabyss.geary.ecs.events.handlers.GearyHandler
import com.mineinabyss.geary.ecs.query.contains
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class QueryManagerTest {
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
            val ResultScope.string by get<String>()
            val int = has<Int>()

            override fun ResultScope.tick() {
                string shouldBe entity.get<String>()
                entity.has<Int>() shouldBe true
            }
        }
        val stringId = componentId<String>() or HOLDS_DATA
        val intId = componentId<Int>()

        val correctArchetype = root + stringId + intId

        init {
            QueryManager.trackQuery(system)
        }

        @Test
        fun `family type is correct`() {
            GearyType(system.family.components).getArchetype() shouldBe root + stringId
        }

        @Test
        fun `archetypes have been matched correctly`() {
            system.matchedArchetypes shouldContain correctArchetype
        }

        @Test
        fun `get entities matching family`() {
            QueryManager.getEntitiesMatching(system.family).apply {
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
            val ResultScope.string by get<String>()

            override fun ResultScope.tick() {
                entity.remove<String>()
                ran++
            }
        }

        init {
            QueryManager.trackQuery(removingSystem)
        }

        //FIXME who needs systems to work properly anyways
        @Test
        fun `concurrent modification`() {
            val entities = (0 until 10).map { Engine.entity { set("Test") } }
            val total =
                QueryManager.getEntitiesMatching(family {
                    hasData<String>()
                }).count()
            removingSystem.doTick()
            ran shouldBe total
            entities.map { it.getComponents() } shouldContainExactly entities.map { setOf() }
        }
    }

    private class RelationTestComponent

    @Test
    fun relations() {
        var ran = 0
        val system = object : TickingSystem() {
            val ResultScope.test by relation<RelationTestComponent>()
            override fun ResultScope.tick() {
                ran++
                family.relationDataTypes.map { it.id } shouldContain test.relation.id
                test.data.shouldBeInstanceOf<RelationTestComponent>()
            }
        }
        system.family.relationDataTypes.shouldContainExactly(RelationDataType(componentId<RelationTestComponent>()))
        QueryManager.trackQuery(system)
        val entity = Engine.entity {
            setRelation<RelationTestComponent, String>(RelationTestComponent())
            add<String>()
        }
        val entity2 = Engine.entity {
            setRelation<RelationTestComponent, Int>(RelationTestComponent())
            add<Int>()
        }
        val entity3 = Engine.entity {
            setRelation<String, RelationTestComponent>("")
            add<RelationTestComponent>()
        }
        family { has(entity.type) }.relationDataTypes.first() shouldBe system.family.relationDataTypes.first()
        system.matchedArchetypes.shouldContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        system.matchedArchetypes.shouldNotContain(entity3.type.getArchetype())

        system.doTick()
        ran shouldBe 2

    }

    private class RelationTestComponent1
    private class RelationTestComponent2

    @Test
    fun relationPermutations() {
        var ran = 0
        val system = object : TickingSystem() {
            val ResultScope.test1 by relation<RelationTestComponent1>()
            val ResultScope.test2 by relation<RelationTestComponent2>()
            override fun ResultScope.tick() {
                ran++
                test1.data.shouldBeInstanceOf<RelationTestComponent1>()
                test2.data.shouldBeInstanceOf<RelationTestComponent2>()
            }
        }
        QueryManager.trackQuery(system)

        Engine.entity {
            setRelation<RelationTestComponent1, String>(RelationTestComponent1())
            setRelation<RelationTestComponent1, Int>(RelationTestComponent1())
            setRelation<RelationTestComponent2, String>(RelationTestComponent2())
            setRelation<RelationTestComponent2, Int>(RelationTestComponent2())
            add<String>()
        }

        system.doTick()

        ran shouldBe 4
    }

    class RelationTestWithData

    @Test
    fun relationsWithData() {
        val system = object : TickingSystem() {
            val ResultScope.withData by relation<Any, RelationTestWithData>()

            override fun ResultScope.tick() {
                withData.value.shouldBeInstanceOf<RelationTestWithData>()
                withData.key shouldBe "Test"
            }
        }

        val entity = Engine.entity {
            setRelation<RelationTestWithData, String>(RelationTestWithData())
            add<String>()
        }

        val entityWithData = Engine.entity {
            setRelation<RelationTestWithData, String>(RelationTestWithData())
            set("Test")
        }

        QueryManager.trackQuery(system)

        system.matchedArchetypes.shouldNotContain(entity.type.getArchetype())
        system.matchedArchetypes.shouldContain(entityWithData.type.getArchetype())

        system.doTick()
    }

    private class TestComponent()
    private object EventListener : GearyListener() {
        var ran = 0
        private val ResultScope.testComponent by get<TestComponent>()

        object Run : GearyHandler() {
            override fun ResultScope.handle(event: EventScope) {
                ran++
            }
        }
    }

    @Test
    fun `empty event handler`() {
        (root.type in EventListener.Run.family) shouldBe true
        Engine.addSystem(EventListener)
        root.eventHandlers shouldContain EventListener.Run
        Engine.entity {
            set(TestComponent())
        }.callEvent()
        // 1 from setting, 1 from calling empty event
        EventListener.ran shouldBe 2
    }
}
