package com.mineinabyss.geary.ecs.serialization

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

object Formats {
    private var module = EmptySerializersModule

    @ExperimentalSerializationApi
    val cborFormat by lazy {
        Cbor {
            serializersModule = module
            encodeDefaults = false
        }
    }

    val jsonFormat by lazy {
        Json {
            serializersModule = module
            useArrayPolymorphism = true
            encodeDefaults = false
        }
    }

    val yamlFormat by lazy {
        Yaml(serializersModule = module, configuration = YamlConfiguration(encodeDefaults = false))
    }

    fun addSerializerModule(module: SerializersModule) {
        this.module += module
    }
}