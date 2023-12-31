package com.mineinabyss.geary.instancing

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.datatypes.Relation
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.tests.GearyTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class InstancingTest: GearyTest() {
    @Test
    fun `should inherit relations`() {
        // arrange
        val relatesTo = entity()
        val prefab = entity {
            addRelation<String>(relatesTo)
        }

        // act
        val instance = entity { extend(prefab) }

        // assert
        instance.getRelations<String?, Any?>().shouldBe(listOf(Relation.of<String?>(relatesTo)))
    }

    @Test
    fun `should inherit set component data`() {
        // arrange
        val prefab = entity {
            set("test")
        }

        // act
        val instance = entity { extend(prefab) }

        // assert
        instance.get<String>() shouldBe "test"
    }

    @Test
    fun `should respect NoInherit relation`() {
        // arrange
        val prefab = entity {
            set("test")
            set(1)
            addRelation<NoInherit, String>()
        }

        // act
        val instance = entity { extend(prefab) }

        // assert
        instance.get<String>().shouldBeNull()
        instance.get<Int>() shouldBe 1
    }
}
