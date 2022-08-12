package com.mineinabyss.geary.engine

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.components.relations.Persists
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.engine.archetypes.Archetype
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.getArchetype
import com.mineinabyss.geary.helpers.tests.GearyTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ArchetypeTest : GearyTest() {
    @Nested
    inner class ArchetypeNavigation {
        @Test
        fun `archetype ids assigned correctly`() {
            engine.rootArchetype.id shouldBe 0
            // Some other archetypes are created on start, but following ones should come in order
            val start = (engine.rootArchetype + 1u).id
            (engine.rootArchetype + 1u + 2u).id shouldBe start + 1
            (engine.rootArchetype + 1u).id shouldBe start
        }

        @Test
        fun `empty type represents empty archetype`() {
            EntityType().getArchetype() shouldBe engine.rootArchetype
        }

        @Test
        fun `getArchetype returns same as manual archetype adding`() {
            engine.rootArchetype + 1u + 2u + 3u - 1u + 1u shouldBe
                    EntityType(listOf(1u, 2u, 3u)).getArchetype()
        }

        @Test
        fun `reach same archetype from different starting positions`() {
            engine.rootArchetype + 1u + 2u + 3u shouldBe engine.rootArchetype + 3u + 2u + 1u
        }
    }

    @Test
    fun matchedRelations() {
        val target = entity()
        val target2 = entity()
        val persists = Relation.of<Persists>(target)
        val instanceOf = Relation.of<InstanceOf?>(target)
        val instanceOf2 = Relation.of<InstanceOf?>(target2)
        val arc = Archetype(
            engine,
            EntityType(listOf(persists.id, instanceOf.id, instanceOf2.id)),
            0
        )
        arc.relationsByTarget[target.id.toLong()].shouldContainExactlyInAnyOrder(persists, instanceOf)
        arc.relationsByKind[componentId<InstanceOf>().toLong()].shouldContainExactlyInAnyOrder(instanceOf, instanceOf2)
    }
}
