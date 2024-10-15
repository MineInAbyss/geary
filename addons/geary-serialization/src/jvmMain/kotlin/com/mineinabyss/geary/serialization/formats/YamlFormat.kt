package com.mineinabyss.geary.serialization.formats

import com.charleskorn.kaml.*
import com.mineinabyss.geary.serialization.formats.Format.ConfigType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import okio.Path
import org.intellij.lang.annotations.Language
import java.io.InputStream

class YamlFormat(
    module: SerializersModule,
    configuration: YamlConfiguration = YamlConfiguration(
        encodeDefaults = false,
        polymorphismStyle = PolymorphismStyle.Property,
        polymorphismPropertyName = "type",
    ),
    nonStrictConfiguration: YamlConfiguration = YamlConfiguration(
        encodeDefaults = false,
        strictMode = false,
        polymorphismStyle = PolymorphismStyle.Property,
        polymorphismPropertyName = "type",
    ),
) : Format {
    override val ext = "yml"

    private val nonStrictYaml = Yaml(
        configuration = nonStrictConfiguration
    )

    val regularYaml = Yaml(
        serializersModule = module,
        configuration = configuration
    )

    override fun <T> decodeFromFile(
        deserializer: DeserializationStrategy<T>,
        path: Path,
        overrideSerializersModule: SerializersModule?,
        configType: ConfigType,
    ): T {
        return decodeFromStream(deserializer, overrideSerializersModule, configType) { path.toFile().inputStream() }
    }

    fun <T> decodeFromStream(
        deserializer: DeserializationStrategy<T>,
        overrideSerializersModule: SerializersModule? = null,
        configType: ConfigType,
        inputStream: () -> InputStream,
    ): T {
        val module = overrideSerializersModule
            ?.let { regularYaml.serializersModule.overwriteWith(it) }
            ?: regularYaml.serializersModule
        val config = getConfig(configType).configuration
        return Yaml(module, config).decodeFromStream(deserializer, inputStream())
    }


    override fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        @Language("yaml") string: String,
        overrideSerializersModule: SerializersModule?,
        configType: ConfigType
    ): T {
        return decodeFromStream(deserializer, overrideSerializersModule, configType) { string.byteInputStream() }
    }

    override fun <T> encodeToFile(
        serializer: SerializationStrategy<T>,
        value: T,
        path: Path,
        overrideSerializersModule: SerializersModule?,
        configType: ConfigType
    ) {
        getConfig(configType).encodeToStream(serializer, value, path.toFile().outputStream())
    }

    fun getConfig(configType: ConfigType): Yaml = when (configType) {
        ConfigType.REGULAR -> regularYaml
        ConfigType.NON_STRICT -> nonStrictYaml
    }
}
