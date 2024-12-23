package com.mineinabyss.geary.instancing

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.modules.relationOf
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InstancingTest : GearyTest() {
    @BeforeEach
    fun reset() { // Order of component init and events we register matters for tests here
        resetEngine()
    }

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
        instance.getRelations<String?, Any?>().shouldBe(listOf(relationOf<String?>(relatesTo)))
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


    @Test
    fun `should correctly extend entity when listener modifies it during extend process`() {
        // arrange
        observe<OnSet>().involving<Int>().exec {
            entity.set("Modifying!")
        }
        val prefab = entity {
            set(1)
            set("test")
        }

        // act
        // order will be set int -> event fires -> set string (because of the creation order of the component ids)
        val instance = entity { extend(prefab) }

        // assert
        instance.get<String>() shouldBe "test"
        instance.get<Int>() shouldBe 1
    }

    @Test
    fun `should not inherit parent prefabs`() {
        val parent = entity()
        val prefab = entity { extend(parent) }
        val entity = entity { extend(prefab) }
        entity.prefabs shouldBe setOf(prefab)
    }
}
