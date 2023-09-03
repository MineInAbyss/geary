package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class QueryManagerTest : GearyTest() {

    private class TestComponent
    private class EventListener : Listener() {
        var ran = 0
        private val Records.testComponent by get<TestComponent>().on(target)

        override fun Records.handle() {
            ran++
        }
    }

    @Test
    fun `empty event handler`() {
        val listener = EventListener()
        geary.pipeline.addSystem(listener)
        (archetypes.archetypeProvider.rootArchetype.type in listener.event.family) shouldBe true
        archetypes.archetypeProvider.rootArchetype.eventListeners shouldContain listener
        entity {
            set(TestComponent())
        }.callEvent()
        // 1 from setting, 1 from calling empty event
        listener.ran shouldBe 2
    }
}
