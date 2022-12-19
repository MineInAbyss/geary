package com.mineinabyss.geary.serialization

import kotlinx.serialization.cbor.Cbor

interface Formats {
    /** Gets a registered [PrefabFormat] for a file with extension [ext]. */
    operator fun get(ext: String): PrefabFormat?

    /** Registers a [PrefabFormat] for a file with extension [ext]. */
    fun register(ext: String, format: PrefabFormat)

    /** The format to use for encoding binary data (usually not to files) */
    val binaryFormat: Cbor
}
