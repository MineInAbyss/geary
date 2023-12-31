package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.di.DI
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PrefabTests {
    private val testKey = PrefabKey.of("test:1")

    @BeforeEach
    fun createEngine() {
        DI.clear()
        geary(TestEngineModule) {
            install(Prefabs)
        }
    }

    @Test
    fun `should not inherit prefab keys by default`() {
        // arrange
        val prefab = entity { set(testKey) }

        // act
        val instance = entity { addPrefab(prefab) }

        // assert
        testKey.toEntity() shouldBe prefab
        instance.get<PrefabKey>().shouldBeNull()
    }

    @Test
    fun `should inherit relations`() {
        // arrange
        val relatesTo = entity()
        val prefab = entity {
            set(testKey)
            addRelation<String>(relatesTo)
        }

        // act
        val instance = entity { addPrefab(prefab) }

        // assert
        instance.getRelations<String, Any?>().shouldBe(listOf(relatesTo))
    }

    @Test
    fun `should track prefabs when key added`(){
        // arrange & act
        val prefab = entity { set(testKey) }

        // assert
        testKey.toEntityOrNull() shouldBe prefab
    }

    @Test
    fun `should respect NoInherit relation`() {
        // arrange
        val prefab = entity {
            set("test")
            set(1)
            addRelation<NoInherit, String>()
            set(testKey)
        }

        // act
        val instance = entity { addPrefab(prefab) }

        // assert
        instance.get<String>().shouldBeNull()
        instance.get<Int>() shouldBe 1
    }
}
