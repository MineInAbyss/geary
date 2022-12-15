package com.mineinabyss.geary.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class QueryManagerTest : GearyTest() {

    private class TestComponent
    private object EventListener : Listener() {
        var ran = 0
        private val TargetScope.testComponent by get<TestComponent>()

        @Handler
        fun handle() {
            ran++
        }
    }

    @Test
    fun `empty event handler`() {
        geary.systems.add(EventListener)
        (geary.archetypeProvider.rootArchetype.type in EventListener.event.family) shouldBe true
        geary.archetypeProvider.rootArchetype.eventHandlers.map { it.parentListener } shouldContain EventListener
        entity {
            set(TestComponent())
        }.callEvent()
        // 1 from setting, 1 from calling empty event
        EventListener.ran shouldBe 2
    }
}
