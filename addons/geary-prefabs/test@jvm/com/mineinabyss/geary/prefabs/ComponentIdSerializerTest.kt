package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentIdSerializerTest {
    interface Components

    @SerialName("test:thing.a")
    @Serializable
    object A : Components

    @SerialName("test:thing.b")
    @Serializable
    object B : Components

    @SerialName("test:subserializers")
    @Serializable
    data class SubSerializers(
        val components: @Serializable(with = PolymorphicListAsMapSerializer::class) List<@Polymorphic Components>
    ) : Components

    val format = YamlFormat(
        SerializersModule {
            polymorphic(Components::class) {
                subclass(A::class)
                subclass(B::class)
                subclass(SubSerializers::class)
            }
        })

    val mapSerializer = PolymorphicListAsMapSerializer(PolymorphicSerializer(Components::class))

    @Test
    fun `should serialize via @Serializable`() {
        val file =
            """
            test:thing.a: {}
            test:thing.b: {}
            """.trimIndent()
        format.decodeFromString(mapSerializer, file) shouldBe listOf(A, B)
    }
}
