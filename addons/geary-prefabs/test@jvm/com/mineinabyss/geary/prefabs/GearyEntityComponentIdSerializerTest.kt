package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.test.GearyTest
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GearyEntityComponentIdSerializerTest : GearyTest() {
    override fun setupGeary() = geary(TestEngineModule) {
        serialization {
            registerComponentSerializers(
                A::class to A.serializer()
            )
        }
    }

    @Serializable
    @SerialName("test:thing.a")
    object A

    @Test
    fun `GearyEntitySerializer should deserialize to entity correctly`() {
        // arrange
        val format = YamlFormat(getAddon(SerializableComponents).formats.module)
        val file =
            """
            test:thing.a: {}
            """.trimIndent()

        // act
        val entity =
            format.decodeFromString(GearyEntitySerializer(), file)

        // assert
        entity.getAll() shouldContainExactly listOf(A)
    }
}
