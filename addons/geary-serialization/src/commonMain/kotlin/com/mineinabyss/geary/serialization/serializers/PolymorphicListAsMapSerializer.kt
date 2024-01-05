package com.mineinabyss.geary.serialization.serializers

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.hasNamespace
import com.mineinabyss.geary.serialization.ProvidedNamespaces
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

open class PolymorphicListAsMapSerializer<T : Any>(
    serializer: KSerializer<T>,
) : KSerializer<List<T>> {
    // We need primary constructor to be a single serializer for generic serialization to work, use of() if manually creating
    private var prefix: String = ""


    val polymorphicSerializer = serializer as? PolymorphicSerializer<T> ?: error("Serializer is not polymorphic")

    val keySerializer = String.serializer()

    @OptIn(InternalSerializationApi::class)
    val valueSerializer = object : KSerializer<Any> {
        override val descriptor = buildSerialDescriptor("GearyComponentSerializer", PolymorphicKind.SEALED)

        override fun deserialize(decoder: Decoder): Any = TODO("Not yet implemented")

        override fun serialize(encoder: Encoder, value: Any): Unit = TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor
        get() = MapSerializer(keySerializer, valueSerializer).descriptor

    fun <T> CompositeDecoder.decodeMapValue(valueSerializer: KSerializer<T>): T {
        val newDescriptor = MapSerializer(keySerializer, valueSerializer).descriptor
        val newIndex = decodeElementIndex(newDescriptor)
        return decodeSerializableElement(newDescriptor, newIndex, valueSerializer)
    }

    override fun deserialize(decoder: Decoder): List<T> {
        val namespaces = getNamespaces(decoder.serializersModule)
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
                    val innerSerializer = of(polymorphicSerializer, key.removeSuffix("*"))
                    components.addAll(compositeDecoder.decodeMapValue(innerSerializer))
                }

                else -> {
                    components += decodeEntry(key, compositeDecoder, namespaces)
                }
            }
        }
        compositeDecoder.endStructure(descriptor)
        return components.toList()
    }

    open fun decodeEntry(key: String, compositeDecoder: CompositeDecoder, namespaces: List<String>): T {
        return compositeDecoder
            .decodeMapValue(findSerializerFor(compositeDecoder.serializersModule, namespaces, key))
    }

    fun getNamespaces(serializersModule: SerializersModule): List<String> {
        return mutableListOf<String>().apply {
            (serializersModule.getContextual(ProvidedNamespaces::class) as? ProvidedNamespaces)?.namespaces
                ?.let { addAll(it) }
        }
    }

    fun findSerializerFor(
        serializersModule: SerializersModule,
        namespaces: List<String>,
        key: String,
    ): KSerializer<T> {
        if (key.startsWith("kotlin.")) {
            return serializersModule.getPolymorphic(polymorphicSerializer.baseClass, key) as KSerializer<T>
        }
        val parsedKey = "$prefix$key".fromCamelCaseToSnakeCase()
        return (if (parsedKey.hasNamespace())
            serializersModule.getPolymorphic(polymorphicSerializer.baseClass, parsedKey)
        else namespaces.firstNotNullOfOrNull { namespace ->
            serializersModule.getPolymorphic(polymorphicSerializer.baseClass, "$namespace:$parsedKey")
        } ?: error("No serializer found for $parsedKey in any of the namespaces $namespaces"))
                as? KSerializer<T> ?: error("Serializer for $parsedKey is not a component serializer")
    }

    override fun serialize(encoder: Encoder, value: List<T>) {
        TODO("Not implemented")
    }

    companion object {
        fun <T : Any> of(
            serializer: PolymorphicSerializer<T>,
            prefix: String = ""
        ):
                PolymorphicListAsMapSerializer<T> {
            return PolymorphicListAsMapSerializer(serializer).apply {
                this.prefix = prefix
            }
        }

        fun ofComponents() = of(PolymorphicSerializer(GearyComponent::class))
    }
}
