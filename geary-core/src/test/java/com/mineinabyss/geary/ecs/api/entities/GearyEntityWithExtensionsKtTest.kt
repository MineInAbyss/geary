package com.mineinabyss.geary.ecs.api.entities

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class GearyEntityWithExtensionsKtTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Test
    fun nullable_with_extensions() {
        println("Creating entity")
        val entity = Engine.entity {
            set("")
            set(1)
        }
        println("Created")

        (entity.with { _: String, _: Int -> true } ?: false) shouldBe true
        (entity.with { _: String, _: Double -> true } ?: false) shouldBe false
        (entity.with { _: String, _: Double? -> true } ?: false) shouldBe true
    }
}
