package com.mineinabyss.serialization.formats

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.serialization.PrefabFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import okio.FileSystem
import okio.Path

class YamlFormat(
    module: SerializersModule
) : PrefabFormat {
    private val yaml = Yaml(
        serializersModule = module,
        configuration = YamlConfiguration(
            encodeDefaults = false
        )
    )

    override fun <T> decodeFromFile(deserializer: DeserializationStrategy<T>, path: Path): T {
        return yaml.decodeFromStream(deserializer, path.toFile().inputStream())
    }

    override fun <T> encodeToFile(serializer: SerializationStrategy<T>, value: T, path: Path) {
        yaml.encodeToStream(serializer, value, path.toFile().outputStream())
    }
}
