package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.relations.Relation
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
        engine.entity { }.id shouldNotBe engine.entity { }.id
    }

    @Test
    fun `adding component to entity`() {
        engine.entity {
            set("Test")
        }.get<String>() shouldBe "Test"
    }

    @Test
    fun `entity has component works via add or set`() {
        val entity = engine.entity {
            add<String>()
            set(1)
        }
        entity.has<String>() shouldBe true
        entity.has<Int>() shouldBe true
    }

    @Test
    fun `entity archetype was set`() {
        engine.entity {
            add<String>()
            set(1)
        }.type.getArchetype(engine) shouldBe engine.root + componentId<String>() + (HOLDS_DATA or componentId<Int>())
    }

    @Test
    fun `component removal`() {
        engine.entity {
            set("Test")
            remove<String>()
        }.type.getArchetype(engine) shouldBe engine.root
    }

    @Test
    fun `add then set`() {
        engine.entity {
            add<String>()
            set("Test")
        }.type.getArchetype(engine) shouldBe engine.root + (componentId<String>() or HOLDS_DATA)
    }

    @Test
    fun getComponents() {
        engine.entity {
            set("Test")
            set(1)
            add<Long>()
        }.getComponents().shouldContainExactly("Test", 1)
    }

    @Test
    fun clear() {
        val entity = engine.entity {
            set("Test")
            add<Int>()
        }
        entity.clear()
        entity.getComponents().isEmpty() shouldBe true
    }

    @Test
    fun setAll() {
        engine.entity {
            setAll(listOf("Test", 1))
            add<Long>()
        }.getComponents().shouldContainExactly("Test", 1)
    }

    @Test
    fun setRelation() {
        val entity = engine.entity {
            setRelation<String, Int>("String to int relation")
        }
        entity.type.shouldContainExactly(
            Relation.of(componentId<String>(), componentId<Int>()).id
        )
        entity.getComponents().shouldContainExactly("String to int relation")
    }

    @Nested
    inner class EntityRemoval {
        @Test
        fun `entity removal and reuse`() {
            //TODO I hate having to do an offset like this, figure out how to reset this engine singleton via reflection
            val offset = engine.getNextId() + 1uL
            repeat(10) {
                engine.entity {
                    add(100uL)
                }
            }

            // We filled up ids 0..9, so next should be at 10
            engine.getNextId() shouldBe offset + 10uL

            (0..9).forEach {
                engine.removeEntity(offset + it.toULong())
            }

            // Since we removed the first 10 entities, the last entity we removed (at 9) should be the next one that's
            // ready to be freed up, then 8, etc...
            engine.getNextId() shouldBe offset + 9uL
            engine.getNextId() shouldBe offset + 8uL
        }
    }
}
