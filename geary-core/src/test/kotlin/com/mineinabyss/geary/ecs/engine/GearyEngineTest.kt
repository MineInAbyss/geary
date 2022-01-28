package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.components.RelationComponent
import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GearyEngineTest : GearyTest() {
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
        }.getComponents().shouldContainExactly("Test", 1)
    }

    @Test
    fun clear() {
        val entity = entity {
            set("Test")
            add<Int>()
        }
        entity.clear()
        entity.getComponents().isEmpty() shouldBe true
    }

    @Test
    fun setAll() {
        entity {
            setAll(listOf("Test", 1))
            add<Long>()
        }.getComponents().shouldContainExactlyInAnyOrder("Test", 1)
    }

    @Test
    fun setRelation() {
        val entity = entity {
            setRelation(Int::class, "String to int relation")
        }
        entity.type.inner.shouldContainExactly(Relation.of<Int, String>().id.toLong())
        entity.getComponents().shouldContainExactly(RelationComponent(componentId<Int>(), "String to int relation"))
    }

    @Nested
    inner class EntityRemoval {
        @Test
        fun `entity removal and reuse`() {
            //TODO I hate having to do an offset like this, figure out how to reset this Engine singleton via reflection
            val offset = entity().id + 1uL
            repeat(10) {
                entity {
                    add(100uL)
                }
            }

            // We filled up ids 0..9, so next should be at 10
            entity().id shouldBe offset + 10uL

            (0..9).forEach {
                engine.removeEntity((offset + it.toULong()).toGeary())
            }

            // Since we removed the first 10 entities, the last entity we removed (at 9) should be the next one that's
            // ready to be freed up, then 8, etc...
            entity().id shouldBe offset + 9uL
            entity().id shouldBe offset + 8uL
        }
    }
}
