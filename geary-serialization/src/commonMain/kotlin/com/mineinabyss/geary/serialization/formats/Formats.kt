package com.mineinabyss.geary.serialization.formats

import com.mineinabyss.geary.serialization.PrefabFormat
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule

interface Formats {
    /** Gets a registered [PrefabFormat] for a file with extension [ext]. */
    operator fun get(ext: String): PrefabFormat?

    /** Registers a [PrefabFormat] for a file with extension [ext]. */
    fun register(ext: String, format: (SerializersModule) -> PrefabFormat)

    /** The format to use for encoding binary data (usually not to files) */
    val binaryFormat: Cbor
}
