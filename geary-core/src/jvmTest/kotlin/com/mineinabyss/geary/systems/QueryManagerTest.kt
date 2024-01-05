package com.mineinabyss.geary.systems

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.accessors.Pointers
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class QueryManagerTest : GearyTest() {

    private class TestComponent
    private class EventListener : Listener() {
        var ran = 0
        private val Records.testComponent by get<TestComponent>().on(target)

        override fun Pointers.handle() {
            ran++
        }
    }

    @Test
    fun `empty event handler`() {
        val listener = EventListener()
        geary.pipeline.addSystem(listener)
        (archetypes.archetypeProvider.rootArchetype.type in listener.event.family) shouldBe true
        entity {
            set(TestComponent())
        }.callEvent()
        listener.ran shouldBe 1
    }
}
