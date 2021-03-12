package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExperimentalUnsignedTypes
internal class GearyEngineTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Test
    fun `engine service set successfully`() {
        Engine.entity { }.id shouldNotBe engine.entity { }.id
    }

    @Test
    fun `adding component to entity`() {
        Engine.entity {
            set("Test")
        }.get<String>() shouldBe "Test"
    }

    @Test
    fun `entity has component works via add or set`() {
        val entity = Engine.entity {
            add<String>()
            set(1)
        }
        entity.has<String>() shouldBe true
        entity.has<Int>() shouldBe true
    }

    @Test
    fun `entity archetype was set`() {
        Engine.entity {
            add<String>()
            set(1)
        }.type.getArchetype() shouldBe root + componentId<String>() + (HOLDS_DATA or componentId<Int>())
    }

    @Test
    fun `component removal`() {
        Engine.entity {
            set("Test")
            remove<String>()
        }.type.getArchetype() shouldBe root
    }

    @Test
    fun `add then set`() {
        Engine.entity {
            add<String>()
            set("Test")
        }.type.getArchetype() shouldBe root + (componentId<String>() or HOLDS_DATA)
    }

    @Test
    fun getComponents() {
        Engine.entity {
            set("Test")
            set(1)
            add<Long>()
        }.getComponents().shouldContainExactly("Test", 1)
    }

    @Test
    fun setAll() {
        Engine.entity {
            setAll(listOf("Test", 1))
            add<Long>()
        }.getComponents().shouldContainExactly("Test", 1)
    }

    @Nested
    inner class EntityRemoval {
        @Test
        fun `entity removal and reuse`() {
            //TODO I hate having to do an offset like this, figure out how to reset this Engine singleton via reflection
            val offset = Engine.getNextId() + 1uL
            repeat(10) {
                Engine.entity {
                    add(100uL)
                }
            }

            // We filled up ids 0..9, so next should be at 10
            Engine.getNextId() shouldBe offset + 10uL

            (0..9).forEach {
                Engine.removeEntity(offset + it.toULong())
            }

            // Since we removed the first 10 entities, the last entity we removed (at 9) should be the next one that's
            // ready to be freed up, then 8, etc...
            Engine.getNextId() shouldBe offset + 9uL
            Engine.getNextId() shouldBe offset + 8uL
        }
    }
}
