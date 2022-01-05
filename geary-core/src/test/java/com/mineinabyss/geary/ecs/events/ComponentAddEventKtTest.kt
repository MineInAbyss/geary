package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.accessors.AffectedScope
import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.allAdded
import com.mineinabyss.geary.ecs.accessors.get
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.Handler
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

    //TODO write test for all methods of checking for added
    inner class  SecondAddMethod: GearyListener() {
        // All three get added
        val AffectedScope.string by get<String>()
        val AffectedScope.int by get<Int>()
        val AffectedScope.double by get<Double>()
        val EventScope.added by allAdded()

        @Handler
        fun increment() {
            inc++
        }
    }

    inner class OnStringAdd : GearyListener() {
        // Either three gets added
        val EventScope.added by allAdded(String::class, Int::class, Double::class)

        @Handler
        fun increment() {
            inc++
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
