package com.mineinabyss.geary.serialization.helpers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor

/**
 * A wrapper around [KSerializer] that only overrides the [descriptor].
 * Not technically needed but doing this just in case.
 */
class SerializerWrapper<T>(override val descriptor: SerialDescriptor, wrapped: KSerializer<T>) :
    KSerializer<T> by wrapped

fun <T> KSerializer<T>.withSerialName(name: String): SerializerWrapper<T> {
    val kind = descriptor.kind
    return if (kind is PrimitiveKind) SerializerWrapper(PrimitiveSerialDescriptor(name, kind), this)
    else SerializerWrapper(SerialDescriptor(name, this.descriptor), this)
}
