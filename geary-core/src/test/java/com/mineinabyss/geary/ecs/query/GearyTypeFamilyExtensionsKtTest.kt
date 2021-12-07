package com.mineinabyss.geary.ecs.query

import com.mineinabyss.geary.ecs.api.relations.Relation
import com.mineinabyss.geary.ecs.api.relations.RelationDataType
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
        val type = sortedSetOf(Relation.of(1uL, 2uL).id, 2uL)
        type.contains(RelationDataType(1uL)) shouldBe true
        type.contains(RelationDataType(2uL)) shouldBe false

        val typeWithoutRelation = sortedSetOf(Relation.of(1uL, 2uL).id)
        typeWithoutRelation.contains(RelationDataType(1uL)) shouldBe false
    }
}
