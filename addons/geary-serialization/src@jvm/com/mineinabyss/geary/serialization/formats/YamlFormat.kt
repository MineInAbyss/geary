package com.mineinabyss.geary.serialization.formats

import com.charleskorn.kaml.*
import com.mineinabyss.geary.serialization.formats.Format.ConfigType
import kotlinx.io.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import java.io.InputStream

class YamlFormat(
    module: SerializersModule,
    configuration: Configuration = Configuration(),
) : Format {
    class Configuration(
        val regular: YamlConfiguration = YamlConfiguration(
            encodeDefaults = false,
            polymorphismStyle = PolymorphismStyle.Property,
            polymorphismPropertyName = "type",
        ),
        val nonStrict: YamlConfiguration = YamlConfiguration(
            encodeDefaults = false,
            strictMode = false,
            polymorphismStyle = PolymorphismStyle.Property,
            polymorphismPropertyName = "type",
        ),
    )
    override val ext = "yml"

    private val nonStrictYaml = Yaml(
        configuration = configuration.nonStrict
    )

    val regularYaml = Yaml(
        serializersModule = module,
        configuration = configuration.regular
    )

    override fun <T> decode(
        deserializer: DeserializationStrategy<T>,
        source: Source,
        overrideSerializersModule: SerializersModule?,
        configType: ConfigType,
    ): T {
        return decodeFromStream(deserializer, overrideSerializersModule, configType) { source.asInputStream() }
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

    override fun <T> encode(
        serializer: SerializationStrategy<T>,
        value: T,
        path: Sink,
        overrideSerializersModule: SerializersModule?,
        configType: ConfigType
    ) {
        getConfig(configType).encodeToStream(serializer, value, path.asOutputStream())
    }

    fun getConfig(configType: ConfigType): Yaml = when (configType) {
        ConfigType.REGULAR -> regularYaml
        ConfigType.NON_STRICT -> nonStrictYaml
    }
}
