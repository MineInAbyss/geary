package com.mineinabyss.geary.systems

import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class RelationMatchingSystemTest : GearyTest() {
    val systemPersists = object : TickingSystem() {
        val TargetScope.persists by relation<Persists, Any>()
        override fun TargetScope.tick() {
            ran++
            family.relationTargetIds.map { it.id } shouldContain test.targetEntity
            test.target.shouldBeInstanceOf<RelationTestComponent>()
        }
    }

    @Test
    fun relations() {
        var ran = 0
        queryManager.trackQuery(systemPersists)
        systemPersists.family.relationTargetIds.shouldContainExactly(RelationValueId(componentId<RelationTestComponent>()))
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
        systemPersists.matchedArchetypes.shouldContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        systemPersists.matchedArchetypes.shouldNotContain(entity3.type.getArchetype())

        engine.cleanup()
        systemPersists.doTick()
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
                withData.kind shouldBe "Test"
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

}
