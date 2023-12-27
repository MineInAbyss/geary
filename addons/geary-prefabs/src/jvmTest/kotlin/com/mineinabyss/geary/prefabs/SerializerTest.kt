package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.prefabs.serializers.PolymorphicListAsMapSerializer
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.jupiter.api.Test

class SerializerTest {
    interface Components

    @SerialName("test:thing.a")
    @Serializable
    object A : Components

    @SerialName("test:thing.b")
    @Serializable
    object B : Components

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(Components::class) {
                subclass(A::class)
                subclass(B::class)
            }
        }
    }

    @Serializable
    class SerializerTest(val components: @Serializable(with = PolymorphicListAsMapSerializer::class) List<@Polymorphic Components>)

    @Test
    fun `should serialize via @Serializable`() {
        json.decodeFromString(
            SerializerTest.serializer(),
            """
            {
                "components": {
                    "test:thing.a": {},
                    "test:thing.b": {}
                }
            }
            """.trimIndent()
        ).components shouldBe listOf(A, B)
    }

    @Test
    fun `should support subkey syntax`() {
        json.decodeFromString(
            SerializerTest.serializer(),
            """
            {
                "components": {
                   "test:thing.*": {
                     "a": {},
                     "b": {}
                   }
                }
            }
            """.trimIndent()
        ).components shouldBe listOf(A, B)
    }
    @Test
    fun `should support importing namespaces`() {
        json.decodeFromString(
            SerializerTest.serializer(),
            """
            {
                "components": {
                    "namespaces": ["test"],
                    "thing.a": {},
                    "thing.b": {}
                }
            }
            """.trimIndent()
        ).components shouldBe listOf(A, B)
    }
}
