package com.mineinabyss.geary.events

import com.mineinabyss.geary.datatypes.Records
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SourceTargetEventTest : GearyTest() {
    class Strength(val amount: Int)
    class Attack
    data class Health(val amount: Int)

    inner class Interaction : Listener() {
        val Records.strength by get<Strength>().on(source)
        var Records.health by get<Health>().on(target)

        init {
            event.mutableFamily.add(family { has<Attack>() })
        }

        override fun Records.handle() {
            health = Health(health.amount - strength.amount)
        }
    }

    @Test
    fun interactions() {
        geary.pipeline.addSystem(Interaction())
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
