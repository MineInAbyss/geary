package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.components.PersistingComponent
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
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
            entity.type.getArchetype().relations[componentId<PersistingComponent>().toLong()]
        relations.size shouldBe 1
        relations.first().component shouldBe (componentId<String>() or HOLDS_DATA)
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
            entity.type.getArchetype().relations[componentId<PersistingComponent>().toLong()]
        relations.size shouldBe 1
        relations.first().component shouldBe (componentId<String>() or HOLDS_DATA)
        entity.getPersistingComponents().shouldContainExactly("Test")
    }

    @Nested
    inner class RelationTest {
        inner class TestRelation;

        @Test
        fun `getRelation reified`() {
            val entity = Engine.entity {
                setRelation<TestRelation, String>(TestRelation(), false)
                add<String>()
            }

            entity.getRelation<TestRelation, String>() shouldBe TestRelation()
        }

        @Test
        fun `getRelation from component`() {
            val stringComponent = "test"
            val entity = Engine.entity {
                setRelation<TestRelation, String>(TestRelation(), false)
                add<String>()
            }

            entity.getRelation<TestRelation>(stringComponent) shouldBe TestRelation()
        }

        @Test
        fun `getRelation from component kClass`() {
            val stringComponent = "test"
            val entity = Engine.entity {
                setRelation<TestRelation, String>(TestRelation(), false)
                add<String>()
            }

            entity.getRelation<TestRelation>(stringComponent::class) shouldBe TestRelation()
        }
    }
}
