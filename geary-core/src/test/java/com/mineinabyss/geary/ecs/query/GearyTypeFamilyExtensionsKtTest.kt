package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.setEngineServiceProvider
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class GearyTypeFamilyExtensionsKtTest {
    val engine: GearyEngine = GearyEngine()

    init {
        setEngineServiceProvider(engine)
    }

    @Test
    fun containsRelation() {
        val type = sortedSetOf(Relation.of(2uL, 1uL).id, 2uL)
        type.containsRelationValue(RelationValueId(1uL)) shouldBe true
        type.containsRelationValue(RelationValueId(2uL)) shouldBe false
    }

    @Test
    fun contains() {
        val type = Engine.entity { setRelation(10uL, "") }.type
        RelationValueLeaf(RelationValueId(componentId<String>())).contains(type) shouldBe true
    }
}
