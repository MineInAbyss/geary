package com.mineinabyss.geary.serialization.formats

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.cbor.Cbor

/**
 * A singleton for accessing different serialization formats with all the registered serializers for [Component]s
 * and more. If anything should be serialized within the ECS, it should be going through one of these serializers.
 *
 * Will likely be converted into a service eventually.
 */
class SimpleFormats(
    override val binaryFormat: Cbor,
    private val formats: Map<String, Format>,
) : Formats {

    override operator fun get(ext: String): Format? = formats[ext]

}
