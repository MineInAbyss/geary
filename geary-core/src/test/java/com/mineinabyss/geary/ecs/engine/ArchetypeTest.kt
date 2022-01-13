package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.components.RelationComponent
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class ArchetypeTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    //TODO bring this back when we switch to Koin DI
//    @Test
//    fun `ids assigned correctly`() {
//        Engine.rootArchetype.id shouldBe 0
//        (Engine.rootArchetype + 1u).id shouldBe 1
//        (Engine.rootArchetype + 1u + 2u).id shouldBe 2
//        (Engine.rootArchetype + 1u).id shouldBe 1
//    }

    @Nested
    inner class MovingBetweenArchetypes {
        @Test
        fun `empty type equals empty archetype`() {
            GearyType().getArchetype() shouldBe Engine.rootArchetype
        }

        @Test
        fun `get type equals archetype adding`() {
            Engine.rootArchetype + 1u + 2u + 3u - 1u + 1u shouldBe
                    GearyType(1u, 2u, 3u).getArchetype()
        }

        @Test
        fun `reach same archetype from different starting positions`() {
            Engine.rootArchetype + 1u + 2u + 3u shouldBe Engine.rootArchetype + 3u + 2u + 1u
        }
    }

    @Test
    fun matchedRelations() {
        val arc = Archetype(
            GearyType(
                Relation.of(1uL or HOLDS_DATA, 10uL).id,
                Relation.of(2uL or HOLDS_DATA, 10uL).id,
            ), 0
        )
        val relation = RelationValueId(10uL)
        val matched = arc.matchedRelationsFor(listOf(relation))
        matched shouldContainKey relation
        matched[relation]?.map { it.key }.shouldContainExactly(1uL or HOLDS_DATA, 2uL or HOLDS_DATA)

        val wrongRelation = RelationValueId(11uL)
        val matched2 = arc.matchedRelationsFor(listOf(wrongRelation))
        matched2 shouldNotContainKey wrongRelation
    }

    @Test
    fun `getComponents with relations`() {
        Engine.entity {
            set("Test")
            setRelation(String::class, 10)
            setRelation(Int::class, 15)
        }.getComponents() shouldContainExactly
                setOf("Test", RelationComponent(componentId<String>(), 10), RelationComponent(componentId<Int>(), 15))
    }
}
