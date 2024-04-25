package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.idofront.di.DI
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CopyToInstancesTest {
    private val testKey = PrefabKey.of("test:1")

    @BeforeEach
    fun createEngine() {
        DI.clear()
        geary(TestEngineModule) {
            install(Prefabs)

            serialization {
                components {
                    component(String.serializer())
                    component(Int.serializer())
                }
            }
        }
        geary.pipeline.runStartupTasks()
    }

    @Test
    fun `should correctly add temporary and persisting components with CopyToInstances`() {
        // arrange
        val prefab = entity {
            set(
                CopyToInstances(
                    temporary = listOf(42),
                    persisting = listOf("Hello world")
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
