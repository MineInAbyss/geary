package com.mineinabyss.geary.queries

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.Comp1
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test

class FamilyMatchingTest : GearyTest() {
    @Test
    fun `andNot should correctly match families when set or added`() {
        // arrange
        resetEngine()
        val a = entity { set("a") }
        val b = entity { add<String>() }
        val one = entity { set(1) }
        val two = entity { add<Int>() }

        // act
        val found = findEntities(family {
            has<String>()
            not { has<Int>() }
        })

        // assert
        found shouldContainExactlyInAnyOrder listOf(a, b)
    }

    @Test
    fun `andNot should correctly match last archetype created`() {
        // arrange
        resetEngine()
        val a = entity {
            add<Comp1>()
            add<String>()
        }
        val b = entity {
            add<Comp1>()
            add<Int>()
        }

        // act
        val entitiesWithoutString = findEntities(family {
            has<Comp1>()
            not { has<String>() }
        })

        // assert
        entitiesWithoutString shouldContainExactlyInAnyOrder listOf(b)
    }
}