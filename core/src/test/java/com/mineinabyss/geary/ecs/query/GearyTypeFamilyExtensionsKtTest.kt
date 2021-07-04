package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GearyTypeFamilyExtensionsKtTest {
    @Test
    fun containsRelation() {
        val type = sortedSetOf(Relation.of(1uL, 2uL).id, 2uL)
        type.contains(RelationParent(1uL)) shouldBe true
        type.contains(RelationParent(2uL)) shouldBe false

        val typeWithoutRelation = sortedSetOf(Relation.of(1uL, 2uL).id)
        typeWithoutRelation.contains(RelationParent(1uL)) shouldBe false
    }
}
