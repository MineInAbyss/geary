package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.datatypes.GearyType
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.helpers.tests.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GearyTypeFamilyExtensionsKtTest : GearyTest() {
    @Test
    fun containsRelation() {
        val type = GearyType(listOf(Relation.of(2uL, 1uL).id, 2uL))
        type.hasRelationTarget(1uL) shouldBe true
        type.hasRelationTarget(2uL) shouldBe false
    }

    @Test
    fun contains() {
        val type = entity { setRelation("", 10uL.toGeary()) }.type
        MutableFamily.Leaf.RelationTarget(componentId<String>()).contains(type) shouldBe true
    }
}
