package com.mineinabyss.geary.systems

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RelationMatchingSystemTest : GearyTest() {
    var ran = 0
    val systemPersists = object : TickingSystem() {
        val TargetScope.persists by relation<Persists, Any?>()

        override fun TargetScope.tick() {
            ran++
            family.relationTargetIds shouldContain persists.targetEntity
            persists.kind.shouldBeInstanceOf<Persists>()
        }
    }

    @Test
    fun relations() = runTest {
        queryManager.trackQuery(systemPersists)
        systemPersists.family.relationTargetIds.shouldContainExactly(componentId<Persists>())
        val entity = entity {
            addRelation<Persists, String>()
            add<String>()
        }
        val entity2 = entity {
            addRelation<Persists, Int>()
            add<Int>()
        }
        val entity3 = entity {
            setRelation<Persists, Int>(Persists())
            add<String>()
        }
        systemPersists.matchedArchetypes.shouldNotContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        systemPersists.matchedArchetypes.shouldContainExactly(entity3.type.getArchetype())

        engine.tick(1)
        ran shouldBe 2
    }

    @Test
    fun relationPermutations() = runTest {
        clearEngine()
        var ran = 0
        val system = object : TickingSystem() {
            val TargetScope.persists by relation<Persists, Any>()
            val TargetScope.instanceOf by relation<InstanceOf?, Any?>()
            override fun TargetScope.tick() {
                ran++
                persists.kind.shouldBeInstanceOf<Persists>()
                persists.target shouldNotBe null
                instanceOf.kind shouldBe null
            }
        }
        queryManager.trackQuery(system)

        entity {
            addRelation<Persists, String>()
            setRelation<Persists, Int>(Persists())
            setRelation<Persists, Short>(Persists())
            setRelation<Persists, Double>(Persists())
            addRelation<InstanceOf, String>()
            addRelation<InstanceOf, Int>()
            set(1)
            set(1.toShort())
            set("")
            add<Double>()
        }

        engine.tick(1)

        // Only two of the Persists relations are valid, times both InstanceOf are valid
        ran shouldBe 2 * 2
    }

    @Test
    fun relationsWithData() = runTest {
        clearEngine()
        val system = object : TickingSystem() {
            val TargetScope.withData by relation<Persists, Any>()

            override fun TargetScope.tick() {
                withData.kind shouldBe Persists()
                withData.target shouldBe "Test"
            }
        }

        val entity = entity {
            setRelation<Persists, String>(Persists())
            add<String>()
        }

        val entityWithData = entity {
            setRelation<Persists, String>(Persists())
            set("Test")
        }

        queryManager.trackQuery(system)

        system.matchedArchetypes.shouldNotContain(entity.type.getArchetype())
        system.matchedArchetypes.shouldContain(entityWithData.type.getArchetype())

        engine.tick(1)
    }
}
