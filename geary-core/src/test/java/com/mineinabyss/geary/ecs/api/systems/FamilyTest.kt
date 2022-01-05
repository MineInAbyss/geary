@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.HOLDS_DATA
import com.mineinabyss.geary.ecs.query.contains
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class FamilyTest {
    @Test
    fun contains() {
        val family = family {
            has(1uL, 2uL, 3uL)
        }
        (sortedSetOf(1uL, 2uL) in family) shouldBe false
        (sortedSetOf(1uL, 2uL, 3uL) in family) shouldBe true
        (sortedSetOf(1uL, 2uL, 3uL, 4uL) in family) shouldBe true
    }

    @Test
    fun `contains relation`() {
        val family = family {
            has(RelationValueId(15uL))
        }
        
        (sortedSetOf(Relation.of(1uL, 14uL).id, 1uL) in family) shouldBe false
        (sortedSetOf(Relation.of(1uL, 15uL).id) in family) shouldBe false
        (sortedSetOf(Relation.of(1uL, 15uL).id, 1uL) in family) shouldBe true
    }

    @Test
    fun `contains relation with data`() {
        val family = family {
            has(RelationValueId(15uL), componentMustHoldData = true)
        }

        (sortedSetOf(Relation.of(1uL, 14uL).id, 1uL) in family) shouldBe false
        (sortedSetOf(Relation.of(1uL, 15uL).id) in family) shouldBe false
        (sortedSetOf(Relation.of(1uL, 15uL).id, 1uL) in family) shouldBe false
        (sortedSetOf(Relation.of(1uL, 15uL).id, 1uL or HOLDS_DATA) in family) shouldBe true
    }
}
