package com.mineinabyss.geary.ecs.serialization

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.ecs.api.GearyComponent
import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.hocon.Hocon
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
    public var module: SerializersModule = EmptySerializersModule
        private set

    internal val addonToModuleMap = mutableMapOf<String, SerializersModule>()

    //TODO allow this to work for all registered classes, not just components
    public fun getClassFor(serialName: String): KClass<out GearyComponent> =
        componentSerialNames[serialName]
            ?: error("$serialName is not a valid component name in the registered components")

    public fun isRegistered(serialName: String): Boolean =
        serialName in componentSerialNames

    /**
     * Adds a class associated with a serial name. Currently haven't found an easy way to get this using serializer
     * modules, but if possible this will be removed.
     */
    public fun registerSerialName(name: String, kClass: KClass<out GearyComponent>) {
        componentSerialNames[name] = kClass
    }

    public lateinit var cborFormat: Cbor

    public lateinit var hoconFormat: Hocon

    public lateinit var jsonFormat: Json

    public lateinit var yamlFormat: Yaml

    public fun createFormats() {
        // Merge modules from all registered addons into one
        module = addonToModuleMap.values.fold(EmptySerializersModule) { acc, module ->
            acc.overwriteWith(module)
        }
        cborFormat = Cbor {
            serializersModule = module
            encodeDefaults = false
        }
        hoconFormat = Hocon {
            serializersModule = module
            useArrayPolymorphism
        }
        jsonFormat = Json {
            serializersModule = module
            useArrayPolymorphism = true
            encodeDefaults = false
        }
        yamlFormat = Yaml(
            serializersModule = module,
            configuration = YamlConfiguration(
                encodeDefaults = false
            )
        )
    }

    //TODO make internal once we switch off of a singleton object
    public fun addSerializerModule(addonName: String, module: SerializersModule) {
        addonToModuleMap[addonName] =
            addonToModuleMap.getOrDefault(addonName, EmptySerializersModule).overwriteWith(module)
    }

    public fun clearSerializerModule(addonName: String) {
        addonToModuleMap -= addonName
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
