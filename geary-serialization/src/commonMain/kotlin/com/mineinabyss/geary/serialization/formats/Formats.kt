package com.mineinabyss.geary.serialization.formats

import com.mineinabyss.geary.serialization.Format
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule

interface Formats {
    /** Gets a registered [Format] for a file with extension [ext]. */
    operator fun get(ext: String): Format?

    /** Registers a [Format] for a file with extension [ext]. */
    fun register(ext: String, format: (SerializersModule) -> Format)

    /** The format to use for encoding binary data (usually not to files) */
    val binaryFormat: Cbor
}
