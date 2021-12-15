package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
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

        private inner class Increment(): ComponentAddHandler() {
            override fun ResultScope.handle(event: EventResultScope) {
                inc++
            }
        }
    }

    @Test
    fun componentAddEvent() {
        val listener = OnStringAdd()
        Engine.addSystem(listener)
        Engine.entity {
            fun addedListeners() = type.getArchetype().eventListeners.count { it === listener }
            set("")
            set(1)
            inc shouldBe 0
            addedListeners() shouldBe 0
            set(1.0)
            addedListeners() shouldBe 1
            inc shouldBe 1

            set(1f)
            addedListeners() shouldBe 1
            inc shouldBe 1
            set("")
            addedListeners() shouldBe 1
            //TODO decide on this behaviour
            inc shouldBe 1
        }
    }
}
