package com.mineinabyss.geary.systems

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.Query
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RelationMatchingSystemTest : GearyTest() {
    @Test
    fun relations() {
        var ran = 0
        resetEngine()
        val system = geary.system(object : Query() {
            val persists by getRelationsWithData<Persists, Any?>()
        }).exec {
            ran++
            persists.forAll { it.data.shouldBeInstanceOf<Persists>() }
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
        system.runner.matchedArchetypes.shouldNotContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        system.runner.matchedArchetypes.shouldContain(entity3.type.getArchetype())

        system.tick()
        ran shouldBe 1
    }

    @Test
    fun relationPermutations() {
        resetEngine()
        var ran = 0
        var persistsCount = 0
        var instanceOfCount = 0
        val system = geary.system(object : Query() {
            val persists by getRelationsWithData<Persists, Any>()
            val instanceOf by getRelationsWithData<InstanceOf?, Any?>()
        }).exec {
            ran++
            persistsCount += persists.size
            instanceOfCount += instanceOf.size
            persists.forAll { it.data.shouldBeInstanceOf<Persists>() }
            persists.forAll { it.targetData shouldNotBe null }
            instanceOf.forAll { it.data shouldBe null }
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

        val system = geary.system(object : Query() {
            val withData by getRelationsWithData<Persists, Any>()
        }).exec {
            withData.forAll { it.data shouldBe Persists() }
            withData.forAll { it.targetData shouldBe "Test" }
        }

        system.runner.matchedArchetypes.shouldNotContain(entity.type.getArchetype())
        system.runner.matchedArchetypes.shouldContain(entityWithData.type.getArchetype())

        geary.engine.tick(0)
    }
}
