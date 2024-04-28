package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.systems.accessors.RelationWithData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GearyEntityTests : GearyTest() {
    private data class RelatesTo(val data: Int = 0)

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
        }.type.getArchetype() shouldBe archetypes.archetypeProvider.rootArchetype + componentId<String>() + componentId<Int>() + (HOLDS_DATA or componentId<Int>())
    }

    @Test
    fun `component removal`() {
        entity {
            set("Test")
            remove<String>()
        }.type.getArchetype() shouldBe archetypes.archetypeProvider.rootArchetype
    }

    @Test
    fun `component removal with two components`() {
        entity {
            set(1)
            set("Test")
            remove<String>()
        }.type.getArchetype() shouldBe archetypes.archetypeProvider.rootArchetype + componentId<Int>() + (HOLDS_DATA or componentId<Int>())
    }

    @Test
    fun `should remove correct component when components with higher component id are also set on entity`() {
        // Arrange
        data class Data1(val id: Int)
        data class Data2(val id: Int)

        // Ensure component order
        componentId<Data1>()
        componentId<Data2>()

        val entity = entity {
            set(Data1(0))
            set(Data2(0))
        }

        // Act
        entity.remove<Data1>()

        // Assert
        entity.getAll() shouldBe setOf(Data2(0))

    }

    @Test
    fun `add then set`() {
        entity {
            add<String>()
            set("Test")
        }.type.getArchetype() shouldBe archetypes.archetypeProvider.rootArchetype + componentId<String>() + (componentId<String>() or HOLDS_DATA)
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
            addRelation<RelatesTo, String>()
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
        entity.getAll().shouldContainExactlyInAnyOrder("Test", 1)
        entity.type.inner shouldContainExactly entitySetAll.type.inner
    }

    @Test
    fun setRelation() {
        val entity = entity {
            setRelation<String, Int>("String to int relation")
        }
        entity.type.inner.shouldContainExactlyInAnyOrder(
            Relation.of<String?, Int>().id,
            Relation.of<String, Int>().id,
        )
    }

    @Test
    fun `getAll with relations`() {
        val prefab = entity()
        val entity = entity {
            set("Test")
            setRelation<RelatesTo, String>(RelatesTo())
            addRelation<InstanceOf>(prefab)
        }
        entity.getAll().shouldContainExactlyInAnyOrder(
            "Test",
            RelationWithData(RelatesTo(), null, relation = Relation.of<RelatesTo, String>().withRole(HOLDS_DATA)),
        )
    }

    @Test
    fun `should correctly remove entities`() {
        val entity = entity { set("Test") }

        entity.exists() shouldBe true
        entity.removeEntity()
        entity.exists() shouldBe false
    }

    @Nested
    inner class ChildTest {
        @Test
        fun `should handle single parent child relation correctly when using addParent`() {
            val parent = entity()
            val child = entity {
                addParent(parent)
            }

            parent.children.shouldContainExactly(child)
            child.parents.shouldContainExactly(parent)
        }
    }

    @Nested
    inner class RelationTest {
        @Test
        fun `getRelation reified`() {
            val relatesTo = RelatesTo()
            val entity = entity {
                setRelation<RelatesTo, String>(relatesTo)
                set(RelatesTo(100))
                add<String>()
            }

            entity.getRelation<RelatesTo, String>() shouldBe relatesTo
        }
    }
}
