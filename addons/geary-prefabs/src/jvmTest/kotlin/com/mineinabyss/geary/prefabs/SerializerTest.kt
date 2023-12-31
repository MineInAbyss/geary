package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.prefabs.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.serialization.formats.YamlFormat
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okio.Path
import okio.Path.Companion.toOkioPath
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.writeText

class SerializerTest {
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

    @Serializable
    class SerializerTest(val components: @Serializable(with = PolymorphicListAsMapSerializer::class) List<@Polymorphic Components>)

    fun String.asTempFile(): Path = Files.createTempFile("tempfiles", ".tmp").apply { writeText(this@asTempFile) }.toOkioPath()
    @Test
    fun `should serialize via @Serializable`() {
        val file =
            """
            {
                "components": {
                    "test:thing.a": {},
                    "test:thing.b": {}
                }
            }
            """.trimIndent().asTempFile()
        format.decodeFromFile(SerializerTest.serializer(), file).components shouldBe listOf(A, B)
    }

    @Test
    fun `should support subkey syntax`() {
        val file =
            """
            {
                "components": {
                   "test:thing.*": {
                     "a": {},
                     "b": {}
                   }
                }
            }
            """.trimIndent().asTempFile()
        format.decodeFromFile(SerializerTest.serializer(), file).components shouldBe listOf(A, B)
    }

    @Test
    fun `should support importing namespaces`() {
        val file =
            """
            {
                "namespaces": ["test"],
                "components": {
                    "thing.a": {},
                    "thing.b": {}
                }
            }
            """.trimIndent().asTempFile()
        format.decodeFromFile(SerializerTest.serializer(), file).components shouldBe listOf(A, B)
    }

    @Test
    fun `should pass namespaces to child serializers`() {
        val file =
            """
            {
                "namespaces": ["test"],
                "components": {
                    "subserializers": {
                        "components": {
                            "thing.a": {}
                        }
                    }
                }
            }
            """.trimIndent().asTempFile()
        format.decodeFromFile(SerializerTest.serializer(), file).components shouldBe listOf(SubSerializers(listOf(A)))
    }
}
