package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class RecordTest : GearyTest() {
    @Test
    fun `create record`() = runTest {
        val record = Record.of(engine.rootArchetype, 5)
        record.archetype shouldBe engine.rootArchetype
        record.row shouldBe 5
        val record2 = Record.of(engine.rootArchetype + 1uL, 6)
        record2.archetype shouldBe engine.rootArchetype + 1uL
        record2.row shouldBe 6
    }
}
