package com.mineinabyss.geary.ecs.serialization

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.geary.ecs.GearyComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlin.reflect.KClass

public object Formats {
    //TODO some immutability
    public val componentSerialNames: MutableMap<String, KClass<out GearyComponent>> = mutableMapOf()
    private var module = EmptySerializersModule

    public fun addSerialName(name: String, kClass: KClass<out GearyComponent>) {
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

    internal fun addSerializerModule(module: SerializersModule) {
        this.module += module
    }
}
