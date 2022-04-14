package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.getArchetype
import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GearyEntityTests : GearyTest() {
    @Test
    fun setPersisting() {
        val entity = entity {
            setPersisting("Test")
        }
        val relations =
            entity.type.getArchetype().relationsByValue[componentId<PersistingComponent>().toLong()]!!
        relations.size shouldBe 1
        relations.first().key shouldBe componentId<String>()
        entity.getPersistingComponents().shouldContainExactly("Test")
    }

    @Test
    fun setAllPersisting() {
        val entity = entity {
            set("Test")
            set(1)
        }
        val entitySetAll = entity {
            setAll(listOf("Test", 1))
        }
        entity.type.inner shouldContainExactly entitySetAll.type.inner
    }

    @Test
    fun clear() {
        val entity = entity {
            setPersisting("Test")
        }
        val relations =
            entity.type.getArchetype().relationsByValue[componentId<PersistingComponent>().toLong()]!!
        relations.size shouldBe 1
        relations.first().key shouldBe (componentId<String>())
        entity.getPersistingComponents().shouldContainExactly("Test")
    }

    @Nested
    inner class RelationTest {
        inner class TestRelation

        @Test
        fun `getRelation reified`() {
            val testData = TestRelation()
            val entity = entity {
                setRelation(String::class, testData)
                add<String>()
            }

            entity.getRelation<String, TestRelation>() shouldBe testData
        }
    }
}
