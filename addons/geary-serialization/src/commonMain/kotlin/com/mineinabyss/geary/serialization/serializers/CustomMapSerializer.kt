package com.mineinabyss.geary.serialization.serializers

import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder

abstract class CustomMapSerializer {
    private val keySerializer = String.serializer()
    val descriptor: SerialDescriptor
        get() = MapSerializer(keySerializer, ContextualSerializer(Any::class)).descriptor

    abstract fun decode(key: String, compositeDecoder: CompositeDecoder)

    fun <T> CompositeDecoder.decodeMapValue(valueSerializer: KSerializer<T>): T {
        val newDescriptor = MapSerializer(keySerializer, valueSerializer).descriptor
        val newIndex = decodeElementIndex(newDescriptor)
        return decodeSerializableElement(newDescriptor, newIndex, valueSerializer)
    }

    fun deserialize(decoder: Decoder) {
        var size = 0
        val compositeDecoder = decoder.beginStructure(descriptor)
        while (true) {
            val index = compositeDecoder.decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break

            val startIndex = size * 2
            val key: String = compositeDecoder.decodeSerializableElement(descriptor, startIndex + index, keySerializer)
            decode(key, compositeDecoder)
            size++
        }
        compositeDecoder.endStructure(descriptor)
    }
}
