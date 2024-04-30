package com.mineinabyss.geary.serialization.formats

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import okio.Path

interface Format {
    val ext: String

    fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String,
        overrideSerializersModule: SerializersModule? = null,
        configType: ConfigType = ConfigType.REGULAR,
    ): T

    fun <T> decodeFromFile(
        deserializer: DeserializationStrategy<T>,
        path: Path,
        overrideSerializersModule: SerializersModule? = null,
        configType: ConfigType = ConfigType.REGULAR,
    ): T

    fun <T> encodeToFile(
        serializer: SerializationStrategy<T>,
        value: T,
        path: Path,
        overrideSerializersModule: SerializersModule? = null,
        configType: ConfigType = ConfigType.REGULAR,
    )

    enum class ConfigType {
        REGULAR,
        NON_STRICT
    }
}
