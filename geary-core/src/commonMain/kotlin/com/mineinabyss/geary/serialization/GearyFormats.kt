package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.GearyComponent
import kotlinx.serialization.cbor.Cbor

/**
 * A singleton for accessing different serialization formats with all the registered serializers for [GearyComponent]s
 * and more. If anything should be serialized within the ECS, it should be going through one of these serializers.
 *
 * Will likely be converted into a service eventually.
 */
public class GearyFormats(
    serializers: GearySerializers
) {
    private val formatMap = mutableMapOf<String, PrefabFormat>()

    /** The format to use for encoding binary data (usually not to files) */
    public val binaryFormat: Cbor by lazy {
        Cbor {
            serializersModule = serializers.module
            encodeDefaults = false
        }
    }

    /** Gets a registered [PrefabFormat] for a file with extension [ext]. */
    public operator fun get(ext: String): PrefabFormat? = formatMap[ext]

    /** Registers a [PrefabFormat] for a file with extension [ext]. */
    public fun register(ext: String, format: PrefabFormat) {
        formatMap[ext] = format
    }
}