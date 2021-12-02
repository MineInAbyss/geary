package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
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
        with(engine.scope) {
            engine.entity {
                it.set("Test")
            }.get<String>() shouldBe "Test"
        }
    }

    @Test
    fun `entity has component works via add or set`() {
        with(engine.scope) {
            val entity = engine.entity {
                it.add<String>()
                it.set(1)
            }
            entity.has<String>() shouldBe true
            entity.has<Int>() shouldBe true
        }
    }

    @Test
    fun `entity archetype was set`() {
        with(engine.scope) {
            engine.entity {
                it.add<String>()
                it.set(1)
            }.type.getArchetype(engine) shouldBe engine.root + componentId<String>() + (HOLDS_DATA or componentId<Int>())
        }
    }

    @Test
    fun `component removal`() {
        with(engine.scope) {
            engine.entity {
                it.set("Test")
                it.remove<String>()
            }.type.getArchetype(engine) shouldBe engine.root
        }
    }

    @Test
    fun `add then set`() {
        with(engine.scope) {
            engine.entity {
                it.add<String>()
                it.set("Test")
            }.type.getArchetype(engine) shouldBe engine.root + (componentId<String>() or HOLDS_DATA)
        }
    }

    @Test
    fun getComponents() {
        with(engine.scope) {
            engine.entity {
                it.set("Test")
                it.set(1)
                it.add<Long>()
            }.getComponents().shouldContainExactly("Test", 1)
        }
    }

    @Test
    fun clear() {
        with(engine.scope) {
            val entity = engine.entity {
                it.set("Test")
                it.add<Int>()
            }
            entity.clear()
            entity.getComponents().isEmpty() shouldBe true
        }
    }

    @Test
    fun setAll() {
        with(engine.scope) {

            engine.entity {
                it.setAll(listOf("Test", 1))
                it.add<Long>()
            }.getComponents().shouldContainExactly("Test", 1)
        }
    }

    @Test
    fun setRelation() {
        with(engine.scope) {

            val entity = engine.entity {
                it.setRelation<String, Int>("String to int relation")
            }
            entity.type.shouldContainExactly(
                Relation.of(componentId<String>(), componentId<Int>()).id
            )
            entity.getComponents().shouldContainExactly("String to int relation")
        }
    }

    @Nested
    inner class EntityRemoval {
        @Test
        fun `entity removal and reuse`() {
            //TODO I hate having to do an offset like this, figure out how to reset this engine singleton via reflection
            val offset = engine.getNextId() + 1uL
            repeat(10) {
                engine.entity {
                    it.add(100uL)
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
