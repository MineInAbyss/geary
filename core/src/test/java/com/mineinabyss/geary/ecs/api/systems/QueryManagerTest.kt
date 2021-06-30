package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.engine.*
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
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
            val QueryResult.string by get<String>()
            val int = has<Int>()

            override fun QueryResult.tick() {
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
            system.family.match.getArchetype() shouldBe correctArchetype
        }

        @Test
        fun `archetypes have been matched correctly`() {
            system.matchedArchetypes shouldContain correctArchetype
        }

        @Test
        fun `get entities matching family`() {
            QueryManager.getEntitiesMatching(system.family).apply {
                shouldContain(entity)
                shouldNotContain(entity2)
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
            val QueryResult.string by get<String>()

            override fun QueryResult.tick() {
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
                    has<String>()
                }).count()
            removingSystem.doTick()
            ran shouldBe total
            entities.map { it.getComponents() } shouldBe entities.map { setOf() }
        }
    }

    private class RelationTestComponent

    @Test
    fun relations() {
        var ran = 0
        val system = object : TickingSystem() {
            val QueryResult.expiry by relation<RelationTestComponent>()
            override fun QueryResult.tick() {
                ran++
                family.relationParents.map { it.id } shouldContain expiry.relation.id
                (expiry.data is RelationTestComponent) shouldBe true
            }
        }
        system.family.relationParents shouldBe sortedSetOf(Relation.of(parent = componentId<RelationTestComponent>()))
        QueryManager.trackQuery(system)
        val entity = Engine.entity {
            setRelation<RelationTestComponent, String>(RelationTestComponent(), data = false)
            add<String>()
        }
        val entity2 = Engine.entity {
            setRelation<RelationTestComponent, Int>(RelationTestComponent(), data = false)
            add<Int>()
        }
        val entity3 = Engine.entity {
            setRelation<String, RelationTestComponent>("", data = false)
            add<RelationTestComponent>()
        }
        family { has(entity.type) }.relationParents.first() shouldBe system.family.relationParents.first()
        system.matchedArchetypes.shouldContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        system.matchedArchetypes.shouldNotContain(entity3.type.getArchetype())

        system.doTick()
        ran shouldBe 2

    }

}
