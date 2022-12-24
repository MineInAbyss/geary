package com.mineinabyss.geary.serialization.formats

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.serialization.Format
import com.mineinabyss.geary.serialization.serializableComponents
import kotlinx.serialization.cbor.Cbor

/**
 * A singleton for accessing different serialization formats with all the registered serializers for [Component]s
 * and more. If anything should be serialized within the ECS, it should be going through one of these serializers.
 *
 * Will likely be converted into a service eventually.
 */
class SimpleFormats : Formats {
    private val serializers = serializableComponents.serializers
    private val formatMap = mutableMapOf<String, Format>()

    override val binaryFormat: Cbor by lazy {
        Cbor {
            serializersModule = serializers.module
            encodeDefaults = false
        }
    }

    override operator fun get(ext: String): Format? = formatMap[ext]

    override fun register(ext: String, format: Format) {
        formatMap[ext] = format
    }
}
