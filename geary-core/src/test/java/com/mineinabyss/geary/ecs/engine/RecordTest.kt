package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class RecordTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Test
    fun `create record`() {
        val record = Record.of(Engine.rootArchetype, 5)
        record.archetype shouldBe Engine.rootArchetype
        record.id shouldBe 5
        val record2 = Record.of(Engine.rootArchetype + 1uL, 6)
        record2.archetype shouldBe Engine.rootArchetype + 1uL
        record2.id shouldBe 5
    }
}
