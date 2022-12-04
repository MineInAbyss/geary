package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.context.geary
import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.cbor.Cbor

/**
 * A singleton for accessing different serialization formats with all the registered serializers for [Component]s
 * and more. If anything should be serialized within the ECS, it should be going through one of these serializers.
 *
 * Will likely be converted into a service eventually.
 */
public class Formats : IFormats {
    private val formatMap = mutableMapOf<String, PrefabFormat>()

    public override val binaryFormat: Cbor by lazy {
        Cbor {
            serializersModule = geary.serializers.module
            encodeDefaults = false
        }
    }

    override operator fun get(ext: String): PrefabFormat? = formatMap[ext]

    override fun register(ext: String, format: PrefabFormat) {
        formatMap[ext] = format
    }
}
