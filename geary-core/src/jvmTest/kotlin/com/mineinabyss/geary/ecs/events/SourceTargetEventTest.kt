package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.GearyTest
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SourceTargetEventTest : GearyTest() {
    class Strength(val amount: Int)
    class Attack
    data class Health(val amount: Int)

    inner class Interaction : GearyListener() {
        val SourceScope.strength by get<Strength>()
        val TargetScope.health by get<Health>()
        val EventScope.attacked by family { has<Attack>() }

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
