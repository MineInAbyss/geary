package com.mineinabyss.geary.serialization.serializers

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.hasNamespace
import com.mineinabyss.geary.serialization.ProvidedNamespaces
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

typealias SerializedComponents = @Serializable(with = PolymorphicListAsMapSerializer::class) List<@Polymorphic GearyComponent>

open class PolymorphicListAsMapSerializer<T : Any>(
    serializer: KSerializer<T>,
) : KSerializer<List<T>> {
    // We need primary constructor to be a single serializer for generic serialization to work, use of() if manually creating
    private var prefix: String = ""


    val polymorphicSerializer = serializer as? PolymorphicSerializer<T> ?: error("Serializer is not polymorphic")

    override val descriptor = MapSerializer(String.serializer(), ContextualSerializer(Any::class)).descriptor

    override fun deserialize(decoder: Decoder): List<T> {
        val components = mutableListOf<T>()

        val mapSerializer = object : CustomMapSerializer() {
            override fun decode(key: String, compositeDecoder: CompositeDecoder) {
                val namespaces = getNamespaces(decoder.serializersModule)
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
                        components += compositeDecoder.decodeMapValue(
                            findSerializerFor(
                                compositeDecoder.serializersModule,
                                namespaces,
                                key
                            )
                        )
                    }
                }
            }
        }
        mapSerializer.deserialize(decoder)
        return components
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
