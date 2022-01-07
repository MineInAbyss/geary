package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.get
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.Handler
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SourceTargetEventTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    class Strength(val amount: Int)
    class Attack()
    data class Health(val amount: Int)

    inner class Interaction : GearyListener() {
        val SourceScope.strength by get<Strength>()
        val TargetScope.health by get<Health>()

        init {
            event.has<Attack>()
        }

        @Handler
        fun damage(source: SourceScope, target: TargetScope, event: EventScope) {
            target.entity.set(Health(target.health.amount - source.strength.amount))
        }
    }

    @Test
    fun interactions() {
        Engine.addSystem(Interaction())
        val source = Engine.entity {
            set(Strength(10))
        }
        val target = Engine.entity {
            set(Health(10))
        }
        target.get<Health>()?.amount shouldBe 10
        target.callEvent(Attack(), source = source)
        target.get<Health>()?.amount shouldBe 0
        target.callEvent(Attack())
        target.get<Health>()?.amount shouldBe 0
        target.callEvent(source = source)
        target.get<Health>()?.amount shouldBe 0
    }
}
