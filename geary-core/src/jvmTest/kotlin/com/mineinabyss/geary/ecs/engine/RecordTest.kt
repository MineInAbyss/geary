package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class RecordTest : GearyTest() {
    @Test
    fun `create record`() {
        val record = Record(engine.rootArchetype, 5)
        record.archetype shouldBe engine.rootArchetype
        record.row shouldBe 5
        val record2 = Record(engine.rootArchetype + 1uL, 6)
        record2.archetype shouldBe engine.rootArchetype + 1uL
        record2.row shouldBe 6
    }
}
