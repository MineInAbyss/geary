package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.getArchetype
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class ComponentAddEventKtTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    var inc = 0

    inner class OnStringAdd : GearyListener() {
        val ResultScope.string by get<String>()
        val ResultScope.int by get<Int>()
        val ResultScope.double by get<Double>()
        override fun GearyHandlerScope.register() {
            onComponentAdd {
                inc++
            }
        }
    }

    @Test
    fun componentAddEvent() {
        val listener = OnStringAdd()
        Engine.addSystem(listener)
        Engine.entity {
            fun addedHandlers() = type.getArchetype().eventHandlers.count { it.holder === listener }
            set("")
            set(1)
            inc shouldBe 0
            addedHandlers() shouldBe 0
            set(1.0)
            addedHandlers() shouldBe 1
            inc shouldBe 1

            set(1f)
            addedHandlers() shouldBe 1
            inc shouldBe 1
            set("")
            addedHandlers() shouldBe 1
            //TODO decide on this behaviour
            inc shouldBe 1
        }
    }
}
