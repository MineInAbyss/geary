package com.mineinabyss.geary.ecs.serialization

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlin.reflect.KClass

/**
 * A singleton for accessing different serialization formats with all the registered serializers for [GearyComponent]s
 * and more. If anything should be serialized within the ECS, it should be going through one of these serializers.
 *
 * Will likely be converted into a service eventually.
 */
public object Formats {
    private val componentSerialNames: MutableBiMap<String, KClass<out GearyComponent>> = mutableBiMapOf()
    private var module = EmptySerializersModule

    //TODO allow this to work for all registered classes, not just components
    public fun getClassFor(serialName: String): KClass<out GearyComponent> =
        componentSerialNames[serialName] ?: error("$serialName is not a valid component name in the registered components")

    public fun isRegistered(serialName: String): Boolean =
        serialName in componentSerialNames

    /**
     * Adds a class associated with a serial name. Currently haven't found an easy way to get this using serializer
     * modules, but if possible this will be removed.
     */
    public fun registerSerialName(name: String, kClass: KClass<out GearyComponent>) {
        componentSerialNames[name] = kClass
    }

    @ExperimentalSerializationApi
    public val cborFormat: Cbor by lazy {
        Cbor {
            serializersModule = module
            encodeDefaults = false
        }
    }

    public val jsonFormat: Json by lazy {
        Json {
            serializersModule = module
            useArrayPolymorphism = true
            encodeDefaults = false
        }
    }

    public val yamlFormat: Yaml by lazy {
        Yaml(serializersModule = module, configuration = YamlConfiguration(encodeDefaults = false))
    }

    //TODO make internal once we switch off of a singleton object
    public fun addSerializerModule(module: SerializersModule) {
        this.module = module.overwriteWith(this.module)
    }

    public fun getSerializerFor(
        key: String,
        baseClass: KClass<*> = GearyComponent::class
    ): DeserializationStrategy<out GearyComponent>? =
        module.getPolymorphic(baseClass = baseClass, serializedClassName = key)

    public inline fun <reified T : GearyComponent> getSerializerFor(): DeserializationStrategy<T>? {
        return getSerializerFor(T::class) as DeserializationStrategy<T>?
    }

    public fun getSerializerFor(kClass: KClass<out GearyComponent>): DeserializationStrategy<out GearyComponent>? {
        val serialName = getSerialNameFor(kClass) ?: return null

        @Suppress("UNCHECKED_CAST") // higher level logic ensures this never fails based on how we register serial names
        return getSerializerFor(serialName, GearyComponent::class)
    }

    public inline fun <reified T : GearyComponent> getSerialNameFor(): String? =
        getSerialNameFor(T::class)

    public fun getSerialNameFor(kClass: KClass<out GearyComponent>): String? =
        componentSerialNames.inverse[kClass]
}
