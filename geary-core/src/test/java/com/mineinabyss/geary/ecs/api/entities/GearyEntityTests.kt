package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.getArchetype
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class GearyEntityTests {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Test
    fun setPersisting() {
        val entity = Engine.entity {
            setPersisting("Test")
        }
        val relations =
            entity.type.getArchetype().relationsByValue[componentId<PersistingComponent>().toLong()]
        relations.size shouldBe 1
        relations.first().key shouldBe componentId<String>()
        entity.getPersistingComponents().shouldContainExactly("Test")
    }

    @Test
    fun setAllPersisting() {
        val entity = Engine.entity {
            set("Test")
            set(1)
        }
        val entitySetAll = Engine.entity {
            setAll(listOf("Test", 1))
        }
        entity.type shouldContainExactly entitySetAll.type
    }

    @Test
    fun clear() {
        val entity = Engine.entity {
            setPersisting("Test")
        }
        val relations =
            entity.type.getArchetype().relationsByValue[componentId<PersistingComponent>().toLong()]
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
            val entity = Engine.entity {
                setRelation(String::class, testData)
                add<String>()
            }

            entity.getRelation<String, TestRelation>() shouldBe testData
        }
    }
}
