package com.mineinabyss.geary.async

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.TickingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ConcurrentSystemModificationTest : GearyTest() {
    var ran = 0

    val removingSystem = object : TickingSystem() {
        val TargetScope.string by get<String>()

        override fun TargetScope.tick() {
            entity.remove<String>()
            ran++
        }
    }

    init {
        clearEngine()
        queryManager.trackQuery(removingSystem)
    }

    @Test
    fun `concurrent modification`() {
        val entities = (0 until 10).map { entity { set("Test") } }
        val total =
            queryManager.getEntitiesMatching(family {
                hasSet<String>()
            }).count()
        removingSystem.doTick()
        ran shouldBe total
        entities.map { it.getAll() } shouldContainExactly entities.map { setOf() }
    }
}
