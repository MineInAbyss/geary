package com.mineinabyss.serialization.formats

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.serialization.ProvidedNamespaces
import com.mineinabyss.geary.serialization.formats.Format
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import okio.Path
import org.intellij.lang.annotations.Language
import java.io.InputStream

class YamlFormat(
    module: SerializersModule
) : Format {
    override val ext = "yml"

    private val propertiesYaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = false,
            strictMode = false,
        )
    )

    val readingYaml = Yaml(
        serializersModule = module,
        configuration = YamlConfiguration(
            encodeDefaults = false,
        )
    )


    @Serializable
    class YamlFileProperties(val namespaces: List<String> = listOf())

    override fun <T> decodeFromFile(deserializer: DeserializationStrategy<T>, path: Path): T {
        return decodeFromStream(deserializer) { path.toFile().inputStream() }
    }

    fun <T> decodeFromStream(deserializer: DeserializationStrategy<T>, inputStream: () -> InputStream): T {
        val fileProperties = propertiesYaml.decodeFromStream(YamlFileProperties.serializer(), inputStream())
        val newModule = readingYaml.serializersModule.overwriteWith(SerializersModule {
            contextual(ProvidedNamespaces::class, ProvidedNamespaces(fileProperties.namespaces))
        })
        return Yaml(newModule, readingYaml.configuration).decodeFromStream(deserializer, inputStream())
    }

    fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, @Language("yaml") string: String): T {
        return decodeFromStream(deserializer) { string.byteInputStream() }
    }

    override fun <T> encodeToFile(serializer: SerializationStrategy<T>, value: T, path: Path) {
        readingYaml.encodeToStream(serializer, value, path.toFile().outputStream())
    }
}
