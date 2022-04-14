package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.building.get
import com.mineinabyss.geary.ecs.api.annotations.Handler
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.api.systems.provideDelegate
import com.mineinabyss.geary.helpers.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SourceTargetEventTest : GearyTest() {
    class Strength(val amount: Int)
    class Attack()
    data class Health(val amount: Int)

    inner class Interaction : GearyListener() {
        val SourceScope.strength by get<Strength>()
        val TargetScope.health by get<Health>()

        override fun onStart() {
            event.has<Attack>()
        }

        @Handler
        fun damage(source: SourceScope, target: TargetScope, event: EventScope) {
            target.entity.set(Health(target.health.amount - source.strength.amount))
        }
    }

    @Test
    fun interactions() {
        engine.addSystem(Interaction())
        val source = entity {
            set(Strength(10))
        }
        val target = entity {
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
