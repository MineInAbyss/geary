package com.mineinabyss.geary.serialization.serializers

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlMap
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.hasNamespace
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder

typealias SerializedComponents = @Serializable(with = PolymorphicListAsMapSerializer::class) List<@Polymorphic GearyComponent>


open class PolymorphicListAsMapSerializer<T : Any>(
    serializer: KSerializer<T>,
) : KSerializer<List<T>> {
    // We need primary constructor to be a single serializer for generic serialization to work, use of() if manually creating
    private var config: Config<T> = Config()

    val polymorphicSerializer = serializer as? PolymorphicSerializer<T> ?: error("Serializer is not polymorphic")

    override val descriptor = MapSerializer(String.serializer(), ContextualSerializer(Any::class)).descriptor

    override fun deserialize(decoder: Decoder): List<T> {
        val components = mutableListOf<T>()
        val componentMap = decoder.decodeSerializableValue(YamlMap.serializer())
        val yaml = (decoder as YamlInput).yaml

        componentMap.entries.forEach { (yamlKey, node) ->
            val key = yamlKey.content
            val componentSerializer = config.customKeys[key]?.invoke()
                ?: findSerializerFor(yaml.serializersModule, emptyList(), key).getOrNull()

            if (componentSerializer == null) {
                if (config.onMissingSerializer != OnMissing.IGNORE) config.whenComponentMalformed(key)
                when (config.onMissingSerializer) {
                    OnMissing.ERROR -> error("Missing serializer for polymorphic key: $key")
                    OnMissing.WARN -> Geary.w("No serializer found for $key, ignoring")
                    OnMissing.IGNORE -> Unit
                }
                return@forEach
            }

            runCatching { yaml.decodeFromYamlNode(componentSerializer, node) }
                .onSuccess { components += it }
                .onFailure {
                    config.whenComponentMalformed(key)
                    if (config.skipMalformedComponents) {
                        Geary.w {
                            "Malformed component $key, ignoring:\n" +
                                    it.stackTraceToString()
                                        .lineSequence()
                                        .joinToString("\n", limit = 10, truncated = "...")
                        }
                    } else throw it
                }
        }

        return components
    }

    fun getParentConfig(serializersModule: SerializersModule): Config<*>? {
        return (serializersModule.getContextual(ProvidedConfig::class) as? ProvidedConfig)?.config
    }

    fun findSerializerFor(
        serializersModule: SerializersModule,
        namespaces: List<String>,
        key: String,
    ): Result<KSerializer<T>> = runCatching {
        val namespaces = namespaces.plus("geary").toSet()
        if (key.startsWith("kotlin.")) {
            return@runCatching serializersModule.getPolymorphic(polymorphicSerializer.baseClass, key) as KSerializer<T>
        }
        val defaultNamespaces = namespaces.plus("geary").toSet()
        val parsedKey = "${config.prefix}$key".fromCamelCaseToSnakeCase()
        return@runCatching (if (parsedKey.hasNamespace())
            serializersModule.getPolymorphic(polymorphicSerializer.baseClass, parsedKey)
        else defaultNamespaces.firstNotNullOfOrNull { namespace ->
            serializersModule.getPolymorphic(polymorphicSerializer.baseClass, "$namespace:$parsedKey")
        } ?: error("No serializer found for $parsedKey in any of the namespaces $defaultNamespaces"))
                as? KSerializer<T> ?: error("Serializer for $parsedKey is not a component serializer")
    }

    override fun serialize(encoder: Encoder, value: List<T>) {
        TODO("Not implemented")
    }

    enum class OnMissing {
        ERROR, WARN, IGNORE
    }

    data class Config<T : Any>(
        val namespaces: List<String> = listOf(),
        val prefix: String = "",
        val onMissingSerializer: OnMissing = OnMissing.WARN,
        val skipMalformedComponents: Boolean = true,
        val whenComponentMalformed: (String) -> Unit = {},
        val customKeys: Map<String, () -> KSerializer<out T>> = mapOf(),
    )

    companion object {
        fun <T : Any> of(
            serializer: PolymorphicSerializer<T>,
            config: Config<T> = Config(),
        ): PolymorphicListAsMapSerializer<T> {
            return PolymorphicListAsMapSerializer(serializer).apply {
                this.config = config
            }
        }

        fun ofComponents(
            config: Config<Any> = Config(),
        ) = of(PolymorphicSerializer(GearyComponent::class)).apply {
            this.config = config
        }

        fun SerializersModuleBuilder.provideConfig(config: Config<*>) {
            contextual(ProvidedConfig::class, ProvidedConfig(config))
        }
    }
}
