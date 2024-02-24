package com.mineinabyss.geary.systems

import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.query.ListenerQuery
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class QueryManagerTest : GearyTest() {

    private class TestComponent

    var ran = 0
    fun myListener() = geary.listener(object : ListenerQuery() {
        val testComponent by get<TestComponent>()
    }).exec { ran++ }

    @Test
    fun `empty event handler`() {
        val listener = myListener()
        (archetypes.archetypeProvider.rootArchetype.type in listener.families.event) shouldBe true
        entity {
            set(TestComponent())
        }.callEvent()
        ran shouldBe 1
    }
}
