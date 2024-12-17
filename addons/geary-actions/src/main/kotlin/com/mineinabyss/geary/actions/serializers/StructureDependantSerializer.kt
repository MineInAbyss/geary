package com.mineinabyss.geary.actions.serializers

import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure

abstract class StructureDependantSerializer<T>: KSerializer<T> {
    // Descriptor is contextual since it depends on the chosen structure
    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    inline fun <T, R> Decoder.tryDecode(
        serializer: KSerializer<T>,
        descriptor: SerialDescriptor = serializer.descriptor,
        crossinline mapOnSuccess: (T) -> R
    ): R? = runCatching {
        decodeStructure(descriptor) {
            val decoded = decodeSerializableElement(descriptor, 0, serializer)
            mapOnSuccess(decoded)
        }
    }.getOrNull()
}
