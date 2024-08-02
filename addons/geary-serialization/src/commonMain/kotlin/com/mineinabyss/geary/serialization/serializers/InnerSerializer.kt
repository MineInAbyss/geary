package com.mineinabyss.geary.serialization.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class InnerSerializer<I, O>(
    val serialName: String,
    val inner: KSerializer<I>,
    val transform: Decoder.(I) -> O,
    val inverseTransform: (O) -> I,
) : KSerializer<O> {
    override val descriptor =
        if (inner.descriptor.kind is PrimitiveKind)
            PrimitiveSerialDescriptor(serialName, inner.descriptor.kind as PrimitiveKind)
        else SerialDescriptor(serialName, inner.descriptor)

    override fun deserialize(decoder: Decoder): O {
        return transform(decoder, inner.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: O) {
        inner.serialize(encoder, inverseTransform(value))
    }
}
