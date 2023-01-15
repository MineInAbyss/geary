package com.mineinabyss.geary.serialization.formats

import kotlinx.serialization.cbor.Cbor

interface Formats {
    /** Gets a registered [Format] for a file with extension [ext]. */
    operator fun get(ext: String): Format?

    /** The format to use for encoding binary data (usually not to files) */
    val binaryFormat: Cbor
}

