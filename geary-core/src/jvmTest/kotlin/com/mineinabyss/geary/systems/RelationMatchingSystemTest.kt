package com.mineinabyss.geary.systems

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RelationMatchingSystemTest : GearyTest() {
    @Test
    fun relations() = runTest {
        clearEngine()
        var ran = 0
        val systemPersists = object : TickingSystem() {
            val TargetScope.persists by getRelations<Persists, Any?>()

            override fun TargetScope.tick() {
                ran++
                persists.data.shouldBeInstanceOf<Persists>()
            }
        }
        engine.addSystem(systemPersists)
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
        (entity3.type in systemPersists.family) shouldBe true
        systemPersists.matchedArchetypes.shouldNotContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        systemPersists.matchedArchetypes.shouldContain(entity3.type.getArchetype())

        engine.tick(0)
        ran shouldBe 1
    }

    @Test
    fun relationPermutations() = runTest {
        clearEngine()
        var ran = 0
        val system = object : TickingSystem() {
            val TargetScope.persists by getRelations<Persists, Any>()
            val TargetScope.instanceOf by getRelations<InstanceOf?, Any?>()
            override fun TargetScope.tick() {
                ran++
                persists.data.shouldBeInstanceOf<Persists>()
                persists.targetData shouldNotBe null
                instanceOf.data shouldBe null
            }
        }
        engine.addSystem(system)

        entity {
            setRelation<Persists, Int>(Persists()) // Yes
            set(1)

            setRelation<Persists, Short>(Persists()) // Yes
            set(1.toShort())

            addRelation<InstanceOf, String>() // Yes
            set("")

            addRelation<InstanceOf, Int>() // Yes

            setRelation<Persists, Double>(Persists()) // No
            add<Double>()

            addRelation<Persists, String>() // No
        }

        engine.tick(0)

        // Only two of the Persists relations are valid, times both InstanceOf are valid
        ran shouldBe 2 * 2
    }

    @Test
    fun relationsWithData() = runTest {
        clearEngine()
        val system = object : TickingSystem() {
            val TargetScope.withData by getRelations<Persists, Any>()

            override fun TargetScope.tick() {
                withData.data shouldBe Persists()
                withData.targetData shouldBe "Test"
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

        engine.tick(0)
    }
}
