package com.mineinabyss.geary.serialization.formats

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import okio.Path

interface Format {
    val ext: String

    fun <T> decodeFromFile(deserializer: DeserializationStrategy<T>, path: Path): T
    fun <T> encodeToFile(serializer: SerializationStrategy<T>, value: T, path: Path)
}
