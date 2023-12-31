package com.mineinabyss.geary.prefabs.serializers

import com.mineinabyss.geary.serialization.ProvidedNamespaces
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


class PolymorphicListAsMapSerializer<T : Any> internal constructor(
    serializer: KSerializer<T>,
) : KSerializer<List<T>> {
    // We need primary constructor to be a single serializer for generic serialization to work, use of() if manually creating
    private var namespaces: List<String> = listOf()
    private var prefix: String = ""


    val polymorphicSerializer = serializer as? PolymorphicSerializer<T> ?: error("Serializer is not polymorphic")

    val keySerializer = String.serializer()
    val valueSerializer = GearyComponentSerializer()

    override val descriptor: SerialDescriptor
        get() = MapSerializer(keySerializer, valueSerializer).descriptor

    fun <T> CompositeDecoder.decodeMapValue(valueSerializer: KSerializer<T>): T {
        val newDescriptor = MapSerializer(keySerializer, valueSerializer).descriptor
        val newIndex = decodeElementIndex(newDescriptor)
        return decodeSerializableElement(newDescriptor, newIndex, valueSerializer)
    }

    override fun deserialize(decoder: Decoder): List<T> {
        val namespaces = mutableListOf<String>().apply {
            (decoder.serializersModule.getContextual(ProvidedNamespaces::class) as? ProvidedNamespaces)?.namespaces
                ?.let { addAll(it) }
//            addAll(namespaces)
        }
        val components = mutableListOf<T>()
        val compositeDecoder = decoder.beginStructure(descriptor)
        while (true) {
            val index = compositeDecoder.decodeElementIndex(descriptor)
            if (index == CompositeDecoder.DECODE_DONE) break

            val startIndex = components.size * 2
            val key: String = compositeDecoder.decodeSerializableElement(descriptor, startIndex + index, keySerializer)
            when {
                key == "namespaces" -> {
                    // Ignore namespaces component, it's parsed as a file-wide property
                    compositeDecoder.decodeMapValue(ListSerializer(String.serializer()))
                }

                key.endsWith("*") -> {
                    val innerSerializer = of(polymorphicSerializer, namespaces, key.removeSuffix("*"))
                    components.addAll(compositeDecoder.decodeMapValue(innerSerializer))
                }

                else -> {
                    val decodedValue =
                        compositeDecoder.decodeMapValue(findSerializerFor(compositeDecoder, namespaces, key))
                    components += decodedValue
                }
            }
        }
        compositeDecoder.endStructure(descriptor)
        return components.toList()
    }

    @OptIn(InternalSerializationApi::class)
    fun findSerializerFor(
        decoder: CompositeDecoder,
        namespaces: List<String>,
        key: String,
    ): KSerializer<T> {
        val parsedKey = "$prefix$key".fromCamelCaseToSnakeCase()
        return (if (parsedKey.hasNamespace()) polymorphicSerializer.findPolymorphicSerializerOrNull(decoder, parsedKey)
        else namespaces.firstNotNullOfOrNull { namespace ->
            polymorphicSerializer.findPolymorphicSerializerOrNull(decoder, "$namespace:$parsedKey")
        } ?: error("No serializer found for $parsedKey in any of the namespaces $namespaces"))
                as? KSerializer<T> ?: error("Serializer for $parsedKey is not a component serializer")
    }

    private fun String.hasNamespace(): Boolean = contains(":")

    override fun serialize(encoder: Encoder, value: List<T>) {
        TODO("Not implemented")
    }

    companion object {
        private val camelRegex = Regex("([A-Z])")
        fun String.fromCamelCaseToSnakeCase(): String {
            return this.replace(camelRegex, "_$1").removePrefix("_").lowercase()
        }


        fun <T : Any> of(
            serializer: PolymorphicSerializer<T>,
            namespaces: List<String> = listOf(),
            prefix: String = ""
        ):
                PolymorphicListAsMapSerializer<T> {
            return PolymorphicListAsMapSerializer(serializer).apply {
                this.namespaces = namespaces
                this.prefix = prefix
            }
        }
    }
}
