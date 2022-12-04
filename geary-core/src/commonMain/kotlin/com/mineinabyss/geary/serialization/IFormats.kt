package com.mineinabyss.geary.serialization

import kotlinx.serialization.cbor.Cbor

public interface IFormats {
    /** Gets a registered [PrefabFormat] for a file with extension [ext]. */
    public operator fun get(ext: String): PrefabFormat?

    /** Registers a [PrefabFormat] for a file with extension [ext]. */
    public fun register(ext: String, format: PrefabFormat)

    /** The format to use for encoding binary data (usually not to files) */
    public val binaryFormat: Cbor
}
