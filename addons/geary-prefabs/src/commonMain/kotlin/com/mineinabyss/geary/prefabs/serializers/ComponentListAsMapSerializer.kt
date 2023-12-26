package com.mineinabyss.geary.prefabs.serializers

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
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

class ComponentListAsMapSerializer(
    val namespaces: List<String> = listOf(),
    val prefix: String = "",
) : KSerializer<List<GearyComponent>> {
    val keySerializer = String.serializer()
    val valueSerializer = GearyComponentSerializer()

    override val descriptor: SerialDescriptor
        get() = MapSerializer(keySerializer, valueSerializer).descriptor

    fun <T> CompositeDecoder.decodeMapValue(valueSerializer: KSerializer<T>): T {
        val newDescriptor = MapSerializer(keySerializer, valueSerializer).descriptor
        val newIndex = decodeElementIndex(newDescriptor)
        return decodeSerializableElement(newDescriptor, newIndex, valueSerializer)
    }

    override fun deserialize(decoder: Decoder): List<GearyComponent> {
        val namespaces = namespaces.toMutableList()
        val components = mutableListOf<GearyComponent>()
        val compositeDecoder = decoder.beginStructure(descriptor)
        while (true) {
            val index = compositeDecoder.decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break

            val startIndex = components.size * 2
            val key: String = compositeDecoder.decodeSerializableElement(descriptor, startIndex + index, keySerializer)
            when {
                key == "namespaces" -> {
                    val valueSerializer = ListSerializer(String.serializer())
                    val namespacesList = compositeDecoder.decodeMapValue(valueSerializer)
                    namespaces.addAll(namespacesList)
                }

                key.endsWith("*") -> {
                    val innerSerializer = ComponentListAsMapSerializer(namespaces, key.removeSuffix("*"))
                    components.addAll(compositeDecoder.decodeMapValue(innerSerializer))
                }

                else -> {
                    val foundValueSerializer = serializableComponents.serializers
                        .getSerializerFor("$prefix$key".fromCamelCaseToSnakeCase(), GearyComponent::class, namespaces) as? KSerializer<Any>
                        ?: error("No component serializer registered for $key")

                    val decodedValue = compositeDecoder.decodeMapValue(foundValueSerializer)
                    components += decodedValue
                }
            }
        }
        compositeDecoder.endStructure(descriptor)
        return components.toList()
    }


    override fun serialize(encoder: Encoder, value: List<GearyComponent>) {
        TODO("Not implemented")
    }

    companion object{
        private val camelRegex = Regex("([A-Z])")
        fun String.fromCamelCaseToSnakeCase(): String {
            return this.replace(camelRegex, "_$1").removePrefix("_").lowercase()
        }
    }
}
