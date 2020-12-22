package com.mineinabyss.geary.ecs.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public interface FlatWrap<A> {
    public val wrapped: A
}

@ExperimentalSerializationApi
public class DescriptorWrapper(override val serialName: String, wrapped: SerialDescriptor) : SerialDescriptor by wrapped

public abstract class FlatSerializer<T : FlatWrap<A>, A : Any>(
        serialName: String,
        private val serializer: KSerializer<A>,
        private val create: (A) -> T
) : KSerializer<T> {
    @ExperimentalSerializationApi
    override val descriptor: SerialDescriptor = DescriptorWrapper(serialName, serializer.descriptor)

    override fun deserialize(decoder: Decoder): T {
        return create(decoder.decodeSerializableValue(serializer))
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeSerializableValue(serializer, value.wrapped)
    }
}
