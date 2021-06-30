package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationParent
import org.junit.jupiter.api.Test

class GearyTypeFamilyExtensionsKtTest {
    @Test
    fun containsRelation() {
        val type = sortedSetOf(Relation.of(1uL, 2uL).id, 2uL)
        assert(RelationParent(1uL) in type)
        assert(RelationParent(2uL) !in type)

        val typeWithoutRelation = sortedSetOf(Relation.of(1uL, 2uL).id)
        assert(RelationParent(1uL) !in typeWithoutRelation)
    }
}
