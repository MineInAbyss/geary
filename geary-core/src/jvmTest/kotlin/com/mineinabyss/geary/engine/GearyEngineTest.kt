package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.withRole
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.TickingSystem
import com.mineinabyss.geary.systems.accessors.RelationWithData
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
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
        }.getAll().shouldContainExactly("Test", 1)
    }

    @Test
    fun clear() {
        val entity = entity {
            set("Test")
            add<Int>()
        }
        entity.clear()
        entity.getAll().isEmpty() shouldBe true
    }

    @Test
    fun setAll() {
        entity {
            setAll(listOf("Test", 1))
            add<Long>()
        }.getAll().shouldContainExactlyInAnyOrder("Test", 1)
    }

    @Test
    fun setRelation() {
        val entity = entity {
            setRelation<String, Int>("String to int relation")
        }
        entity.type.inner.shouldContainExactly(Relation.of<Int, String>().id)
    }

    @Test
    fun `getAll with relations`() {
        val prefab = entity()
        entity {
            set("Test")
            setRelation<Persists, String>(Persists())
            addRelation<InstanceOf>(prefab)
        }.getAll().shouldContainExactly(
            "Test",
            RelationWithData(Persists(), null, relation = Relation.of<Persists, String>().withRole(HOLDS_DATA)),
            Relation.of<InstanceOf>(prefab)
        )
    }

    @Nested
    inner class EntityRemoval {
        @Test
        fun `entity removal and reuse`() {
            val offset = entity().id + 1uL
            repeat(100) {
                entity()
            }

            // We filled up ids 0..9, so next should be at 10
            entity().id shouldBe offset + 100uL

            (0 until 100).forEach {
                engine.removeEntity((offset + it.toULong()).toGeary())
            }

            // Since we removed the first 10 entities, the last entity we removed (at 9) should be the next one that's
            // ready to be freed up, then 8, etc...
            //TODO it looks like entities get removed fine but we can't await them properly with a concurrent map
//            repeat(100) {
//                (offset..(offset + 100uL)).toSet().shouldContain(entity().id)
//            }
        }
    }

    class CheckAsyncSystem : TickingSystem() {
        val TargetScope.string by get<String>()
        override fun TargetScope.tick() {
            error("Found entity with string when it should have been removed before iteration")
        }
    }

    //TODO figure out what's up here
//    @Test
    fun runSafely() = runTest {
        clearEngine()
        engine.addSystem(CheckAsyncSystem())
        launch {
            repeat(5000) {
                engine.tick(it.toLong())
            }
        }
        concurrentOperation(50000) {
            runSafely {
                entity {
                    set("Hello world")
                }.removeEntity()
            }.await()
        }.awaitAll()
    }
}
