package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GearyTypeFamilyHelpersTest : GearyTest() {
    @Test
    fun containsRelation() {
        val type = EntityType(listOf(Relation.of(2uL, 1uL).id, 2uL))
        type.hasRelationTarget(1uL) shouldBe true
        type.hasRelationTarget(2uL) shouldBe false
    }

    @Test
    fun contains() {
        val type = entity { setRelation("", 10uL.toGeary()) }.type
        MutableFamily.Leaf.KindToAny(componentId<String>(), false).contains(type) shouldBe true
        MutableFamily.Leaf.KindToAny(componentId<String>(), true).contains(type) shouldBe false
    }
}
