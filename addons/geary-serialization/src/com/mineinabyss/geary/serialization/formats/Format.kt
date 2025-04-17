package com.mineinabyss.geary.serialization.formats

import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule

interface Format {
    val ext: String

//    fun <T> decodeFromString(
//        deserializer: DeserializationStrategy<T>,
//        string: String,
//        overrideSerializersModule: SerializersModule? = null,
//        configType: ConfigType = ConfigType.REGULAR,
//    ): T

    fun <T> decode(
        deserializer: DeserializationStrategy<T>,
        source: Source,
        overrideSerializersModule: SerializersModule? = null,
        configType: ConfigType = ConfigType.REGULAR,
    ): T

    fun <T> encode(
        serializer: SerializationStrategy<T>,
        value: T,
        sink: Sink,
        overrideSerializersModule: SerializersModule? = null,
        configType: ConfigType = ConfigType.REGULAR,
    )

    enum class ConfigType {
        REGULAR,
        NON_STRICT
    }

    fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String,
        overrideSerializersModule: SerializersModule? = null,
        configType: ConfigType = ConfigType.REGULAR,
    ): T {
        val buffer = Buffer().apply { writeString(string) }
        return decode(deserializer, buffer, overrideSerializersModule, configType)
    }
}


fun main() {
    SystemFileSystem
}
