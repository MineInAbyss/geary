package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.helpers.GearyTest
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
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
            entity.type.getArchetype().relationsByTarget[componentId<Persists>().toLong()]!!
        relations.size shouldBe 1
        relations.first().kind shouldBe componentId<String>()
        entity.getAllPersisting().shouldContainExactly("Test")
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
            entity.type.getArchetype().relationsByTarget[componentId<Persists>().toLong()]!!
        relations.size shouldBe 1
        relations.first().kind shouldBe (componentId<String>())
        entity.getAllPersisting().shouldContainExactly("Test")
    }

    @Nested
    inner class RelationTest {
        inner class TestRelation

        @Test
        fun `getRelation reified`() {
            val testData = TestRelation()
            val entity = entity {
                setRelation<TestRelation, String>(testData)
                add<String>()
            }

            //FIXME test is wrong
            entity.getRelation<String, TestRelation>() shouldBe testData
        }
    }
}
