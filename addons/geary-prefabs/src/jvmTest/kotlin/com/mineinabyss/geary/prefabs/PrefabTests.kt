package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
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
        val instance = entity { extend(prefab) }

        // assert
        testKey.toEntity() shouldBe prefab
        instance.get<PrefabKey>().shouldBeNull()
    }

    @Test
    fun `should track prefabs when key added`() {
        // arrange & act
        val prefab = entity { set(testKey) }

        // assert
        testKey.toEntityOrNull() shouldBe prefab
    }
}
