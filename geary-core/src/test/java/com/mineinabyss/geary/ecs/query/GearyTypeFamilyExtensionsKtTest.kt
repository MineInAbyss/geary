package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GearyTypeFamilyExtensionsKtTest {
    @Test
    fun containsRelation() {
        val type = sortedSetOf(Relation.of(1uL, 2uL).id, 2uL)
        type.contains(RelationDataType(1uL)) shouldBe true
        type.contains(RelationDataType(2uL)) shouldBe false

        val typeWithoutRelation = sortedSetOf(Relation.of(1uL, 2uL).id)
        typeWithoutRelation.contains(RelationDataType(1uL)) shouldBe false
    }
}
