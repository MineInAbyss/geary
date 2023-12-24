package com.mineinabyss.geary.prefabs.serializers

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


class GearyComponentSerializer : KSerializer<GearyComponent> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("GearyComponentSerializer", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): GearyComponent {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: GearyComponent) {
        TODO("Not yet implemented")
    }

}

class ComponentListAsMapSerializer : KSerializer<List<GearyComponent>> {
    val keySerializer = String.serializer()
    val valueSerializer = GearyComponentSerializer()

    override val descriptor: SerialDescriptor
        get() = MapSerializer(keySerializer, valueSerializer).descriptor

    override fun deserialize(decoder: Decoder): List<GearyComponent> {
        val components = mutableSetOf<GearyComponent>()
        val compositeDecoder = decoder.beginStructure(descriptor)
        while (true) {
            val index = compositeDecoder.decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break

            val startIndex = components.size * 2
            val key: String = compositeDecoder.decodeSerializableElement(descriptor, startIndex + index, keySerializer)
            val foundValueSerializer =
                serializableComponents.serializers.getSerializerFor(key, GearyComponent::class) as? KSerializer<Any>
                    ?: error("No component serializer registered for $key")
            val newDescriptor = MapSerializer(keySerializer, foundValueSerializer).descriptor
            val newIndex = compositeDecoder.decodeElementIndex(newDescriptor)
            val decodedValue = compositeDecoder.decodeSerializableElement<Any>(
                descriptor = newDescriptor,
                index = newIndex,
                deserializer = foundValueSerializer,
            )
            components += decodedValue
        }
        compositeDecoder.endStructure(descriptor)
        return components.toList()
    }

    override fun serialize(encoder: Encoder, value: List<GearyComponent>) {
        TODO("Not implemented")
    }
}
