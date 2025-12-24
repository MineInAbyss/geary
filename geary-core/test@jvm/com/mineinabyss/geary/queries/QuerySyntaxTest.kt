package com.mineinabyss.geary.queries

import com.mineinabyss.geary.datatypes.HOLDS_DATA
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QuerySyntaxTest : GearyTest() {
    @BeforeEach
    fun setup() {
        resetEngine()
        repeat(10) {
            entity { set<Int>(it) }
        }
    }

    @Test
    fun `should correctly match entities with cached query`() {
        val intComp = componentId<Int>() or HOLDS_DATA

        cache(query { hasSet<Int>() }).forEachArchetype { archetype ->
            val intData = archetype.componentData[archetype.indexOf(intComp)]
            archetype.forEachRow {
                intData[it] = 100
            }
        }
        findEntities(family { has<Int>() }).fastForEach { entity ->
            entity.get<Int>() shouldBe 100
        }
    }
}