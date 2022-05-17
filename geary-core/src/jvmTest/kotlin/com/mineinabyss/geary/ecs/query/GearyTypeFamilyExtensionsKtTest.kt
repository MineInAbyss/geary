package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.datatypes.GearyType
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.datatypes.RelationValueId
import com.mineinabyss.geary.datatypes.family.MutableFamily
import com.mineinabyss.geary.helpers.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GearyTypeFamilyExtensionsKtTest : GearyTest() {
    @Test
    fun containsRelation() {
        val type = GearyType(listOf(Relation.of(2uL, 1uL).id, 2uL))
        type.hasRelationTarget(RelationValueId(1uL)) shouldBe true
        type.hasRelationTarget(RelationValueId(2uL)) shouldBe false
    }

    @Test
    fun contains() {
        val type = entity { setRelation("", 10uL) }.type
        MutableFamily.Leaf.RelationValue(RelationValueId(componentId<String>())).contains(type) shouldBe true
    }
}
