package com.mineinabyss.geary.systems

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.RecordPointer
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.Pointer
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.collections.shouldNotContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

//TODO reimplement relations matching
class RelationMatchingSystemTest : GearyTest() {
    @Test
    fun relations() {
        resetEngine()
        val system = object: RepeatingSystem() {
            val Pointer.persists by getRelationsWithData<Persists, Any?>()

            var ran = 0

            override fun Pointer.tick() {
                ran++
                persists.forAll { it.data.shouldBeInstanceOf<Persists>() }
            }
        }
        geary.pipeline.addSystem(system)

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
        (entity3.type in system.family) shouldBe true
        system.matchedArchetypes.shouldNotContainAll(entity.type.getArchetype(), entity2.type.getArchetype())
        system.matchedArchetypes.shouldContain(entity3.type.getArchetype())

        geary.engine.tick(0)
        system.ran shouldBe 1
    }

    @Test
    fun relationPermutations() {
        resetEngine()
        var ran = 0
        var persistsCount = 0
        var instanceOfCount = 0
        val system = object : RepeatingSystem() {
            val RecordPointer.persists by getRelationsWithData<Persists, Any>()
            val RecordPointer.instanceOf by getRelationsWithData<InstanceOf?, Any?>()

            override fun RecordPointer.tick() {
                ran++
                persistsCount += persists.size
                instanceOfCount += instanceOf.size
                persists.forAll { it.data.shouldBeInstanceOf<Persists>() }
                persists.forAll { it.targetData shouldNotBe null }
                instanceOf.forAll { it.data shouldBe null }
            }
        }
        geary.pipeline.addSystem(system)

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

        geary.engine.tick(0)

        // Only two of the Persists relations are valid, times both InstanceOf are valid
        ran shouldBe 1
        persistsCount shouldBe 2
        instanceOfCount shouldBe 2
    }

    @Test
    fun relationsWithData() {
        resetEngine()
        val system = object : RepeatingSystem() {
            val RecordPointer.withData by getRelationsWithData<Persists, Any>()

            override fun RecordPointer.tick() {
                withData.forAll { it.data shouldBe Persists() }
                withData.forAll { it.targetData shouldBe "Test" }
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

        geary.queryManager.trackQuery(system)

        system.matchedArchetypes.shouldNotContain(entity.type.getArchetype())
        system.matchedArchetypes.shouldContain(entityWithData.type.getArchetype())

        geary.engine.tick(0)
    }
}
