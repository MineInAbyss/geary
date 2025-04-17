package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.serialization.getAllPersisting
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.test.GearyTest
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test

class CopyToInstancesTest : GearyTest() {
    override fun setupGeary() = geary(TestEngineModule) {
        serialization {
            components {
                component(String.serializer())
                component(Int.serializer())
            }
        }

        install(Prefabs)
    }

    @Test
    fun `should correctly add temporary and persisting components with CopyToInstances`() {
        // arrange
        val prefab = entity {
            set(
                CopyToInstances(
                    temporary = listOf(42),
                    persisting = listOf("Hello world"),
                )
            )
            addRelation<NoInherit, CopyToInstances>()
        }

        // act
        val instance = entity { extend(prefab) }

        // assert
        assertSoftly(instance) {
            get<String>() shouldBe "Hello world"
            get<Int>() shouldBe 42
            getAllPersisting() shouldBe listOf("Hello world")
        }
    }
}
