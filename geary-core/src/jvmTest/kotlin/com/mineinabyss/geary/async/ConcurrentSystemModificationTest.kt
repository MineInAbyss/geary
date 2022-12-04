package com.mineinabyss.geary.async

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ConcurrentSystemModificationTest : GearyTest() {
    //TODO put back when we support concurrency lol
//    @Test
    fun `concurrent modification`() = runTest {
        clearEngine()
        var ran = 0
        val removingSystem = object : RepeatingSystem() {
            val TargetScope.string by get<String>()

            override fun TargetScope.tick() {
                entity.remove<String>()
                ran++
            }
        }
        geary.systems.add(removingSystem)
        val entities = (0 until 10).map { entity { set("Test") } }
        val total =
            queryManager.getEntitiesMatching(family {
                hasSet<String>()
            }).count()
        geary.engine.tick(0)
        ran shouldBe total
        entities.map { it.getAll() } shouldContainExactly entities.map { setOf() }
    }
}
