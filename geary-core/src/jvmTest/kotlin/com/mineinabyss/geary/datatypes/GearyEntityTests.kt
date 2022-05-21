package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.accessors.RelationWithData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GearyEntityTests : GearyTest() {
    @Test
    fun `adding component to entity`() {
        entity {
            set("Test")
        }.get<String>() shouldBe "Test"
    }

    @Test
    fun `entity has component works via add or set`() {
        val entity = entity {
            add<String>()
            set(1)
        }
        entity.has<String>() shouldBe true
        entity.has<Int>() shouldBe true
    }

    @Test
    fun `entity archetype was set`() {
        entity {
            add<String>()
            set(1)
        }.type.getArchetype() shouldBe engine.rootArchetype + componentId<String>() + (HOLDS_DATA or componentId<Int>())
    }

    @Test
    fun `component removal`() {
        entity {
            set("Test")
            remove<String>()
        }.type.getArchetype() shouldBe engine.rootArchetype
    }

    @Test
    fun `add then set`() {
        entity {
            add<String>()
            set("Test")
        }.type.getArchetype() shouldBe engine.rootArchetype + (componentId<String>() or HOLDS_DATA)
    }

    @Test
    fun getComponents() {
        entity {
            set("Test")
            set(1)
            add<Long>()
        }.getAll().shouldContainExactly("Test", 1)
    }

    @Test
    fun clear() {
        val entity = entity {
            set("Test")
            add<Int>()
            addRelation<Persists, String>()
        }
        entity.clear()
        entity.getAll().isEmpty() shouldBe true
    }

    @Test
    fun setAll() {
        val entity = entity {
            set("Test")
            set(1)
        }
        val entitySetAll = entity {
            setAll(listOf("Test", 1))
        }
        entity.type.inner.shouldContainExactlyInAnyOrder("Test", 1)
        entity.type.inner shouldContainExactly entitySetAll.type.inner
    }

    @Test
    fun setRelation() {
        val entity = entity {
            setRelation<String, Int>("String to int relation")
        }
        entity.type.inner.shouldContainExactly(Relation.of<Int, String>().id)
    }

    @Test
    fun `getAll with relations`() {
        val prefab = entity()
        entity {
            set("Test")
            setRelation<Persists, String>(Persists())
            addRelation<InstanceOf>(prefab)
        }.getAll().shouldContainExactly(
            "Test",
            RelationWithData(Persists(), null, relation = Relation.of<Persists, String>().withRole(HOLDS_DATA)),
            Relation.of<InstanceOf>(prefab)
        )
    }

    @Test
    fun setPersisting() {
        val entity = entity {
            setPersisting("Test")
        }
        val relations =
            entity.type.getArchetype().relationsByTarget[componentId<Persists>().toLong()]!!
        relations.size shouldBe 1
        relations.first().kind shouldBe componentId<String>()
        entity.getAllPersisting().shouldContainExactly("Test")
    }

    @Nested
    inner class RelationTest {
        @Test
        fun `getRelation reified`() {
            val persists = Persists()
            val entity = entity {
                setRelation<Persists, String>(persists)
                set(Persists(100))
                add<String>()
            }

            entity.getRelation<Persists, String>() shouldBe persists
        }
    }
}
