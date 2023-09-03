package com.mineinabyss.geary.async

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class ConcurrentSystemModificationTest : GearyTest() {
    //TODO put back when we support concurrency lol
//    @Test
    fun `concurrent modification`() = runTest {
        resetEngine()
        var ran = 0
        val removingSystem = object : RepeatingSystem() {
            var Pointer.string by getRemovable<String>()

            override fun Pointer.tick() {
                string = null
                ran++
            }
        }
        geary.pipeline.addSystem(removingSystem)
        val entities = (0 until 10).map { entity { set("Test") } }
        val total =
            geary.queryManager.getEntitiesMatching(family {
                hasSet<String>()
            }).count()
        geary.engine.tick(0)
        ran shouldBe total
        entities.map { it.getAll() } shouldContainExactly entities.map { setOf() }
    }
}
