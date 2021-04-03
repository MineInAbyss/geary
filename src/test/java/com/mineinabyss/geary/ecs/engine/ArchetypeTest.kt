package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.systems.Family
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
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
                Relation(10uL, 1uL or HOLDS_DATA).id,
                Relation(10uL, 2uL or HOLDS_DATA).id,
            )
        )
        val relation = Relation(10uL, 0uL or HOLDS_DATA)
        val matched = arc.matchedRelationsFor(Family(relations = sortedSetOf(relation)))
        matched shouldContainKey relation.parent
        matched[relation.parent]?.map { it.component }.shouldContainExactly(1uL or HOLDS_DATA, 2uL or HOLDS_DATA)

        val wrongRelation = Relation(11uL, 0uL or HOLDS_DATA)
        val matched2 = arc.matchedRelationsFor(Family(relations = sortedSetOf(wrongRelation)))
        matched2 shouldNotContainKey wrongRelation.id
    }
}
