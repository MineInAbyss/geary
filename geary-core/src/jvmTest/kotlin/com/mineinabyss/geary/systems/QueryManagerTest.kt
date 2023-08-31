package com.mineinabyss.geary.systems

import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.Records
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class QueryManagerTest : GearyTest() {

    private class TestComponent
    private object EventListener : Listener() {
        var ran = 0
        private val Records.testComponent by get<TestComponent>().onTarget()

        override fun Records.handle() {
            ran++
        }
    }

    @Test
    fun `empty event handler`() {
        geary.pipeline.addSystem(EventListener)
        (archetypes.archetypeProvider.rootArchetype.type in EventListener.event.family) shouldBe true
        archetypes.archetypeProvider.rootArchetype.eventListeners shouldContain EventListener
        entity {
            set(TestComponent())
        }.callEvent()
        // 1 from setting, 1 from calling empty event
        EventListener.ran shouldBe 2
    }
}
