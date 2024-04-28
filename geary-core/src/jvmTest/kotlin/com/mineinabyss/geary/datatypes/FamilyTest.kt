package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class FamilyTest: GearyTest() {
    private sealed class RelatesTo

    @Test
    fun contains() {
        val family = family {
            has(1uL, 2uL, 3uL)
        }
        (EntityType(listOf(1uL, 2uL)) in family) shouldBe false
        (EntityType(listOf(1uL, 2uL, 3uL)) in family) shouldBe true
        (EntityType(listOf(1uL, 2uL, 3uL, 4uL)) in family) shouldBe true
    }

    @Test
    fun `contains relation`() {
        val target = entity()
        val other = entity()
        val family = family {
            hasRelation<RelatesTo?>(target)
        }

        (EntityType(listOf(Relation.of<RelatesTo?>(other).id)) in family) shouldBe false
        (EntityType(listOf(Relation.of<RelatesTo?>(target).id)) in family) shouldBe true
        // Archetypes will always have a non HOLDS_DATA version of a component present, but this alone should not succeed
        (EntityType(listOf(Relation.of<RelatesTo>(target).id)) in family) shouldBe false
        (EntityType(listOf(Relation.of<RelatesTo?>(target).id, target.id)) in family) shouldBe true
    }

    @Test
    fun `contains relation with data`() {
        val target = entity()
        val other = entity()
        val family = family {
            hasRelation<RelatesTo>(target)
        }

        (EntityType(listOf(Relation.of<RelatesTo?>(target).id)) in family) shouldBe false
        (EntityType(listOf(Relation.of<RelatesTo?>(other).withRole(HOLDS_DATA).id)) in family) shouldBe false
        (EntityType(listOf(Relation.of<RelatesTo?>(target).withRole(HOLDS_DATA).id)) in family) shouldBe true
    }

    @Test
    fun `complicated family`() {
        val target = entity()
        val family = family {
            has<String>()
            or {
                has<Int>()
                hasRelation<RelatesTo?>(target)
            }
            not {
                has<Double>()
            }
        }

        (EntityType(listOf(Relation.of<RelatesTo?>(target).id)) in family) shouldBe false
        (EntityType(listOf(componentId<String>(), Relation.of<RelatesTo?>(target).id)) in family) shouldBe true

        (EntityType(listOf(componentId<String>())) in family) shouldBe false
        (EntityType(listOf(componentId<Int>())) in family) shouldBe false
        (EntityType(listOf(componentId<String>(), componentId<Int>())) in family) shouldBe true

        (EntityType(listOf(componentId<String>(), componentId<Int>())) in family) shouldBe true

        (EntityType(listOf(componentId<String>(), componentId<Int>(), componentId<Double>())) in family) shouldBe false
    }
}
