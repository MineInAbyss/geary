package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PrefabTests : GearyTest() {
    private val testKey = PrefabKey.of("test:1")

    override fun setupGeary() = geary(TestEngineModule) {
        install(SerializableComponents)
        install(Prefabs)
    }

    @Test
    fun `should not inherit prefab keys by default`() {
        // arrange
        val prefab = entity { set(testKey) }

        // act
        val instance = entity { extend(prefab) }

        // assert
        entityOfOrNull(testKey) shouldBe prefab
        instance.get<PrefabKey>().shouldBeNull()
    }

    @Test
    fun `should track prefabs when key added`() {
        // arrange & act
        val prefab = entity { set(testKey) }

        // assert
        entityOfOrNull(testKey) shouldBe prefab
    }
}
