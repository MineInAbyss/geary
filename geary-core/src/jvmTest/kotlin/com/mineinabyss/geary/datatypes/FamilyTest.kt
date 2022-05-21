@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.mineinabyss.geary.datatypes

import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.helpers.entity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class FamilyTest {
    @Test
    fun contains() {
        val family = family {
            has(1uL, 2uL, 3uL)
        }
        (GearyType(listOf(1uL, 2uL)) in family) shouldBe false
        (GearyType(listOf(1uL, 2uL, 3uL)) in family) shouldBe true
        (GearyType(listOf(1uL, 2uL, 3uL, 4uL)) in family) shouldBe true
    }

    @Test
    fun `contains relation`() {
        val target = entity()
        val other = entity()
        val family = family {
            hasRelation<Persists?>(target)
        }

        (GearyType(listOf(Relation.of<Persists>(other).id)) in family) shouldBe false
        (GearyType(listOf(Relation.of<Persists>(target).id)) in family) shouldBe true
        (GearyType(listOf(Relation.of<Persists>(target).withRole(HOLDS_DATA).id)) in family) shouldBe true
        (GearyType(listOf(Relation.of<Persists>(target).id, target.id)) in family) shouldBe true
    }

    @Test
    fun `contains relation with data`() {
        val target = entity()
        val other = entity()
        val family = family {
            hasRelation<Persists>(target)
        }

        (GearyType(listOf(Relation.of<Persists>(target).id)) in family) shouldBe false
        (GearyType(listOf(Relation.of<Persists>(other).withRole(HOLDS_DATA).id)) in family) shouldBe false
        (GearyType(listOf(Relation.of<Persists>(target).withRole(HOLDS_DATA).id)) in family) shouldBe true
    }

    @Test
    fun `complicated family`() {
        val target = entity()
        val family = family {
            has<String>()
            or {
                has<Int>()
                hasRelation<Persists?>(target)
            }
            not {
                has<Double>()
            }
        }

        (GearyType(listOf(Relation.of<Persists>(target).id)) in family) shouldBe false
        (GearyType(listOf(componentId<String>(), Relation.of<Persists>(target).id)) in family) shouldBe true

        (GearyType(listOf(componentId<String>())) in family) shouldBe false
        (GearyType(listOf(componentId<Int>())) in family) shouldBe false
        (GearyType(listOf(componentId<String>(), componentId<Int>())) in family) shouldBe true

        (GearyType(listOf(componentId<String>(), componentId<Int>())) in family) shouldBe true

        (GearyType(listOf(componentId<String>(), componentId<Int>(), componentId<Double>())) in family) shouldBe false
    }
}
