package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ArchetypeTest {
    @Nested
    inner class MovingBetweenArchetypes {
        @Test
        fun `empty type equals empty archetype`() {
            GearyType().getArchetype() shouldBe root
        }

        @Test
        fun `get type equals archetype adding`() {
            root + 1u + 2u + 3u - 1u + 1u shouldBe sortedSetOf<GearyComponentId>(1u, 2u, 3u).getArchetype()
        }

        @Test
        fun `reach same archetype from different starting positions`() {
            root + 1u + 2u + 3u shouldBe root + 3u + 2u + 1u
        }
    }

    @Test
    fun matchedRelations() {
        val arc = Archetype(
            sortedSetOf(
                Relation.of(1uL or HOLDS_DATA, 10uL).id,
                Relation.of(2uL or HOLDS_DATA, 10uL).id,
            )
        )
        val relation = RelationValueId(10uL)
        val matched = arc.matchedRelationsFor(listOf(relation))
        matched shouldContainKey relation
        matched[relation]?.map { it.key }.shouldContainExactly(1uL or HOLDS_DATA, 2uL or HOLDS_DATA)

        val wrongRelation = RelationValueId(11uL)
        val matched2 = arc.matchedRelationsFor(listOf(wrongRelation))
        matched2 shouldNotContainKey wrongRelation
    }
}
