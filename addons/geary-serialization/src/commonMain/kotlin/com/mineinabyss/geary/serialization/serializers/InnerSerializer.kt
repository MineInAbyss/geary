package com.mineinabyss.geary.serialization.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class InnerSerializer<I, O>(
    val serialName: String,
    val inner: KSerializer<I>,
    val transform: (I) -> O,
    val inverseTransform: (O) -> I,
) : KSerializer<O> {
    override val descriptor = SerialDescriptor(serialName, inner.descriptor)

    override fun deserialize(decoder: Decoder): O {
        return transform(inner.deserialize(decoder))
    }

    override fun serialize(encoder: Encoder, value: O) {
        inner.serialize(encoder, inverseTransform(value))
    }
}
