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
        val fileProperties = propertiesYaml.decodeFromStream(YamlFileProperties.serializer(), path.toFile().inputStream())
        val newModule = readingYaml.serializersModule.overwriteWith(SerializersModule {
            contextual(ProvidedNamespaces::class, ProvidedNamespaces(fileProperties.namespaces))
        })
        return Yaml(newModule, readingYaml.configuration).decodeFromStream(deserializer, path.toFile().inputStream())
    }

    override fun <T> encodeToFile(serializer: SerializationStrategy<T>, value: T, path: Path) {
        readingYaml.encodeToStream(serializer, value, path.toFile().outputStream())
    }
}
