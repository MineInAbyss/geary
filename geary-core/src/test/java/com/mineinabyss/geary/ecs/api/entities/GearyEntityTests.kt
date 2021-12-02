package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.getArchetype
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GearyEntityTests {
    val engine: GearyEngine = GearyEngine()

    @Test
    fun setPersisting() {
        val entity = engine.entity {
            it.setPersisting("Test")
        }
        with(engine.scope) {
            val relations =
                entity.type.getArchetype(engine).relations[componentId<PersistingComponent>().toLong()]
            relations.size shouldBe 1
            relations.first().key shouldBe componentId<String>()
            entity.getPersistingComponents().shouldContainExactly("Test")
        }
    }

    @Test
    fun setAllPersisting() {
        val entity = engine.entity {
            it.set("Test")
            it.set(1)
        }
        val entitySetAll = engine.entity {
            it.setAll(listOf("Test", 1))
        }
        with(engine.scope) {
            entity.type shouldContainExactly entitySetAll.type
        }
    }

    @Test
    fun clear() {
        val entity = engine.entity {
            it.setPersisting("Test")
        }
        with(engine.scope) {
            val relations =
                entity.type.getArchetype(engine).relations[componentId<PersistingComponent>().toLong()]
            relations.size shouldBe 1
            relations.first().key shouldBe (componentId<String>())
            entity.getPersistingComponents().shouldContainExactly("Test")
        }
    }

    @Nested
    inner class RelationTest {
        inner class TestRelation

        @Test
        fun `getRelation reified`() {
            val testData = TestRelation()
            val entity = engine.entity {
                it.setRelation<TestRelation, String>(testData)
                it.add<String>()
            }

            with(engine.scope) {
                entity.getRelation<TestRelation, String>() shouldBe testData
            }
        }
    }
}
