package com.mineinabyss.geary.queries

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.test.GearyTest
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FamilyMatchingTest : GearyTest() {
    @Test
    fun `should correctly match andNot families`() {
        // arrange
        resetEngine()
        val a = entity { set("a") }
        val b = entity { set("b") }
        val one = entity { set(1) }
        val two = entity { set(2) }

        // act
        val found = findEntities(family {
            has<String>()
            not { has<Int>() }
        })

        // assert
        found shouldContainExactlyInAnyOrder listOf(a, b)
    }

    @Test
    fun `should correctly match andNot families when prefab has a non-inherited component`() {
        resetEngine()
        // arrange
        val prefab = entity {
            set("a")
            set(1)
            addRelation<NoInherit, Int>()
        }
        val instance = entity {
            extend(prefab)
        }

        // act
        val componentsOnA = instance.getAll()
        val entitiesWithStringWithoutInt = findEntities(family {
            has<String>()
            not { has<Int>() }
        })
        val entitiesWithIntAndString = findEntities(family {
            has<String>()
            has<Int>()
        })
        val entitiesWithInt = findEntities(family {
            has<Int>()
        })

        // assert
        assertSoftly {
            componentsOnA shouldBe listOf("a")
            entitiesWithStringWithoutInt shouldContainExactlyInAnyOrder listOf(instance)
            entitiesWithIntAndString shouldContainExactlyInAnyOrder listOf(prefab)
            entitiesWithInt shouldContainExactlyInAnyOrder listOf(prefab)
        }
    }
}