package com.mineinabyss.geary.systems

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.findEntities
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.geary.test.GearyTest
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RelationMatchingSystemTest : GearyTest() {
    private class Persists

    @Test
    fun relations() {
        var ran = 0
        resetEngine()
        val system = system(object : Query(this) {
            val persists by getRelationsWithData<Persists, Any?>()
        }).exec { q ->
            ran++
            q.persists.forAll { it.data.shouldBeInstanceOf<Persists>() }
        }

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
        (entity3.type in system.runner.family) shouldBe true
        system.runner.matchedArchetypes.asList().shouldNotContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        system.runner.matchedArchetypes.asList().shouldContain(entity3.type.getArchetype())

        system.tick()
        ran shouldBe 1
    }

    @Test
    fun relationPermutations() {
        resetEngine()
        var ran = 0
        var persistsCount = 0
        var instanceOfCount = 0
        val system = system(object : Query(this) {
            val persists by getRelationsWithData<Persists, Any>()
            val instanceOf by getRelationsWithData<InstanceOf?, Any?>()
        }).exec { q ->
            ran++
            persistsCount += q.persists.size
            instanceOfCount += q.instanceOf.size
            q.persists.forAll { it.data.shouldBeInstanceOf<Persists>() }
            q.persists.forAll { it.targetData shouldNotBe null }
            q.instanceOf.forAll { it.data shouldBe null }
        }

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

        system.tick()

        // Only two of the Persists relations are valid, times both InstanceOf are valid
        ran shouldBe 1
        persistsCount shouldBe 2
        instanceOfCount shouldBe 2
    }

    @Test
    fun relationsWithData() {
        resetEngine()

        val entity = entity {
            setRelation<Persists, String>(Persists())
            add<String>()
        }

        val entityWithData = entity {
            setRelation<Persists, String>(Persists())
            set("Test")
        }

        val system = system(object : Query(this) {
            val withData by getRelationsWithData<Persists, Any>()
        }).exec { q ->
            q.withData.forAll { it.data shouldBe Persists() }
            q.withData.forAll { it.targetData shouldBe "Test" }
        }
        println(componentId<Any>())
        println(findEntities {
            hasRelation<Persists, Any>()
        }.toList())
        system.runner.matchedArchetypes.asList().shouldNotContain(entity.type.getArchetype())
        system.runner.matchedArchetypes.asList().shouldContain(entityWithData.type.getArchetype())

        engine.tick()
    }
}
