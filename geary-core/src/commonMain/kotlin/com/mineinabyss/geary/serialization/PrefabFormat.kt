package com.mineinabyss.geary.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import okio.Path

public interface PrefabFormat {
    public fun <T> decodeFromFile(deserializer: DeserializationStrategy<T>, path: Path): T
    public fun <T> encodeToFile(serializer: SerializationStrategy<T>, value: T, path: Path)
}
