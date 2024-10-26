package com.mineinabyss.geary.serialization.dsl.builders

import com.mineinabyss.geary.serialization.ComponentSerializers
import com.mineinabyss.geary.serialization.formats.Format
import com.mineinabyss.geary.serialization.formats.Formats
import com.mineinabyss.geary.serialization.formats.SimpleFormats
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule

data class FormatsBuilder(
    val formats: MutableMap<String, (SerializersModule) -> Format> = mutableMapOf()
) {
    /** Registers a [Format] for a file with extension [ext]. */
    fun register(ext: String, makeFromat: (SerializersModule) -> Format) {
        formats[ext] = makeFromat
    }

    fun build(serializers: ComponentSerializers): Formats {

        return SimpleFormats(
            binaryFormat = Cbor {
                serializersModule = serializers.module
                encodeDefaults = false
                ignoreUnknownKeys = true
            },
            formats = formats.mapValues { it.value(serializers.module) },
        )
    }
}
