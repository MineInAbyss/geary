package com.mineinabyss.geary.serialization.formats

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.serialization.PrefabFormat
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
    private val formatMap = mutableMapOf<String, PrefabFormat>()

    override val binaryFormat: Cbor by lazy {
        Cbor {
            serializers.serializers
            serializersModule = geary.serializers.module
            encodeDefaults = false
        }
    }

    override operator fun get(ext: String): PrefabFormat? = formatMap[ext]

    override fun register(ext: String, format: PrefabFormat) {
        formatMap[ext] = format
    }
}
