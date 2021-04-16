package com.mineinabyss.geary.ecs.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** A class that holds only one value that we want to delegate serialization to. */
public interface FlatWrap<A> {
    public val wrapped: A
}

/** A wrapper around [SerialDescriptor] that only overrides the [serialName]. */
@ExperimentalSerializationApi
internal class DescriptorWrapper(override val serialName: String, wrapped: SerialDescriptor) :
    SerialDescriptor by wrapped

/**
 * A wrapper around [KSerializer] that only overrides the [descriptor].
 * Not technically needed but doing this just in case.
 */
@ExperimentalSerializationApi
internal class SerializerWrapper<T>(override val descriptor: SerialDescriptor, wrapped: KSerializer<T>) :
    KSerializer<T> by wrapped

/**
 * An abstract serializer for a [FlatWrap] that will use the [wrapped][FlatWrap.wrapped] value's serializer to
 * serialize this class.
 *
 * @param serializer The wrapped class' serializer, type can be inferenced from just `serializer()`
 * @param create How to create the [FlatWrap] class given the wrapped value
 */
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
