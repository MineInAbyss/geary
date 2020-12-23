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
private class DescriptorWrapper(override val serialName: String, wrapped: SerialDescriptor) : SerialDescriptor by wrapped

//not technically needed but doing this just in case
@ExperimentalSerializationApi
private class SerializerWrapper<T>(override val descriptor: SerialDescriptor, wrapped: KSerializer<T>) : KSerializer<T> by wrapped

public abstract class FlatSerializer<T : FlatWrap<A>, A : Any>(
        serialName: String,
        serializer: KSerializer<A>,
        private val create: (A) -> T
) : KSerializer<T> {
    @ExperimentalSerializationApi
    final override val descriptor: SerialDescriptor = DescriptorWrapper(serialName, serializer.descriptor)
    private val wrappedSerializer = SerializerWrapper(descriptor, serializer)

    override fun deserialize(decoder: Decoder): T {
        return create(decoder.decodeSerializableValue(wrappedSerializer))
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeSerializableValue(wrappedSerializer, value.wrapped)
    }
}
