package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
}
