package com.mineinabyss.geary.async

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.helpers.toGeary
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.measureTime

class AsyncArchetypeTests : GearyTest() {
    @Test
    fun `add entities concurrently`() = runTest {
        clearEngine()
        val arc = engine.archetypeProvider.getArchetype(EntityType(ulongArrayOf(componentId<String>() or HOLDS_DATA)))
        concurrentOperation(10000) {
            val rec = engine.getRecord(engine.newEntity())
            arc.addEntityWithData(rec, arrayOf("Test"), rec.entity)
        }.awaitAll()
        arc.entities.size shouldBe 10000
        arc.entities.shouldBeUnique()
    }


    // The two tests below are pretty beefy and more like benchmarks so they're disabled by default
    @Test
    fun `set and remove concurrency`() = runTest {
        println(measureTime {
            concurrentOperation(100) {
                val entity = entity()
                concurrentOperation(1000) { id ->
                    entity.setRelation("String", id.toULong().toGeary())
                }.awaitAll()
                println("Finished for ${entity.id}, arc size ${engine.archetypeProvider.count}")
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
                engine.archetypeProvider.getArchetype(EntityType((0uL..i.toULong()).toList()))
                println("Creating arc $i, total: ${engine.archetypeProvider.count}")
//            }.awaitAll()
            }
        })
        engine.archetypeProvider.count shouldBe iters + 1
    }
}
