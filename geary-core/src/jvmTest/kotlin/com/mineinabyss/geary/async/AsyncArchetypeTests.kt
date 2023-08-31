package com.mineinabyss.geary.async

// TODO we're going to drop general async support in favor of async systems, rewrite tests to reflect that
//class AsyncArchetypeTests : GearyTest() {
//    private val concurrentEntityAmount = 10000
//
////    @Test
//    fun `add entities concurrently`() = runTest {
//        val arc = archetypes.archetypeProvider.getArchetype(EntityType(ulongArrayOf(componentId<String>() or HOLDS_DATA)))
//        concurrentOperation(concurrentEntityAmount) {
//            val rec = archetypes.records[geary.entityProvider.create()]
//            arc.moveWithNewComponent(rec, arrayOf("Test"), rec.entity)
//        }.awaitAll()
//        arc.entities.size shouldBe concurrentEntityAmount
//        arc.entities.shouldBeUnique()
//    }
//
//
//    // The two tests below are pretty beefy and more like benchmarks so they're disabled by default
//    //TODO move into benchmark and turn back into concurrent version when we actually support concurrency
////    @Test
//    fun `set and remove concurrency`() = runTest {
//        println(measureTime {
//            repeat(1000000) {
//                val entity = entity()
//                repeat(0) { id ->
//                    entity.setRelation("String", id.toULong().toGeary())
//                }//.awaitAll()
////                    println("Finished for ${entity.id}, arc size ${engine.archetypeProvider.count}")
//            }//.awaitAll()
//        })
////        entity.getComponents().shouldBeEmpty()
//    }
//
//    //    @Test
////    fun `mutliple locks`() {
////        val a = entity()
//////        val b = entity()
////        concurrentOperation(10000) {
////            engine.withLock(setOf(a/*, b*/)) {
////                println("Locking")
////                delay(100)
////            }
////        }
////    }
//
//    //    @Test
//    fun `concurrent archetype creation`() = runTest {
//        clearEngine()
//        val iters = 10000
//        println(measureTime {
//            for (i in 0 until iters) {
////            concurrentOperation(iters) { i ->
//                archetypes.archetypeProvider.getArchetype(EntityType((0uL..i.toULong()).toList()))
//                println("Creating arc $i, total: ${archetypes.archetypeProvider.count}")
////            }.awaitAll()
//            }
//        })
//        archetypes.archetypeProvider.count shouldBe iters + 1
//    }
//}
