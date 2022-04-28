package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.components.RelationComponent
import com.mineinabyss.geary.datatypes.GearyType
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.RelationValueId
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.GearyTest
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.measureTime

internal class ArchetypeTest : GearyTest() {
    //TODO bring this back when we switch to Koin DI
//    @Test
//    fun `ids assigned correctly`() {
//        Engine.rootArchetype.id shouldBe 0
//        (Engine.rootArchetype + 1u).id shouldBe 1
//        (Engine.rootArchetype + 1u + 2u).id shouldBe 2
//        (Engine.rootArchetype + 1u).id shouldBe 1
//    }

    @Nested
    inner class MovingBetweenArchetypes {
        @Test
        fun `empty type equals empty archetype`() {
            GearyType().getArchetype() shouldBe engine.rootArchetype
        }

        @Test
        fun `get type equals archetype adding`() {
            engine.rootArchetype + 1u + 2u + 3u - 1u + 1u shouldBe
                    GearyType(listOf(1u, 2u, 3u)).getArchetype()
        }

        @Test
        fun `reach same archetype from different starting positions`() {
            engine.rootArchetype + 1u + 2u + 3u shouldBe engine.rootArchetype + 3u + 2u + 1u
        }
    }

    @Test
    fun matchedRelations() {
        val arc = Archetype(
            engine,
            GearyType(
                listOf(
                    Relation.of(1uL or HOLDS_DATA, 10uL).id,
                    Relation.of(2uL or HOLDS_DATA, 10uL).id,
                )
            ), 0
        )
        val relation = RelationValueId(10uL)
        val matched = arc.matchedRelationsFor(listOf(relation))
        matched shouldContainKey relation
        matched[relation]?.map { it.key }.shouldContainExactly(1uL or HOLDS_DATA, 2uL or HOLDS_DATA)

        val wrongRelation = RelationValueId(11uL)
        val matched2 = arc.matchedRelationsFor(listOf(wrongRelation))
        matched2 shouldNotContainKey wrongRelation
    }

    @Test
    fun `getComponents with relations`() {
        entity {
            set("Test")
            setRelation(String::class, 10)
            setRelation(Int::class, 15)
        }.getComponents() shouldContainExactly
                setOf("Test", RelationComponent(componentId<String>(), 10), RelationComponent(componentId<Int>(), 15))
    }

    @Nested
    inner class Async {
        @Test
        fun `add entities concurrently`() = runTest {
            clearEngine()
            val arc = engine.getArchetype(GearyType(ulongArrayOf(componentId<String>() or HOLDS_DATA)))
            concurrentOperation(10000) {
                arc.addEntityWithData(engine.newEntity().getRecord(), arrayOf("Test"))
            }.awaitAll()
            arc.ids.size shouldBe 10000
            arc.ids.shouldBeUnique()
        }
    }


    // The two tests below are pretty beefy and more like benchmarks so they're disabled by default
//    @Test
    fun `set and remove concurrency`() = runTest {
        println(measureTime {
            concurrentOperation(100) {
                val entity = entity()
                repeat(1000) { id ->
                    launch {
//                        entity.withLock {
                        entity.setRelation(id.toULong(), "String")
                        println("Locked for ${entity.id}: $id, size ${engine.archetypeCount}")
//                        }
                    }
                }
            }.awaitAll()
        })
//        entity.getComponents().shouldBeEmpty()
    }

    //    @Test
//    fun `mutliple locks`() {
//        val a = entity()
////        val b = entity()
//        concurrentOperation(10000) {
//            engine.withLock(setOf(a/*, b*/)) {
//                println("Locking")
//                delay(100)
//            }
//        }
//    }

    //    @Test
    fun `concurrent archetype creation`() = runTest {
        clearEngine()
        val iters = 10000
        println(measureTime {
            for (i in 0 until iters) {
//            concurrentOperation(iters) { i ->
                engine.getArchetype(GearyType((0uL..i.toULong()).toList()))
                println("Creating arc $i, total: ${engine.archetypeCount}")
//            }.awaitAll()
            }
        })
        engine.archetypeCount shouldBe iters + 1
    }
}
