package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

interface ComponentSerializers {
    val module: SerializersModule

    //TODO allow this to work for all registered classes, not just components
    fun getClassFor(serialName: String, namespaces: List<String> = listOf()): KClass<out Component>

    fun <T : Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>
    ): DeserializationStrategy<T>?

    fun <T : Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<T>?
    fun getSerialNameFor(kClass: KClass<out Component>): String?

    fun <T : Any> getKClassFor(serializer: KSerializer<T>): KClass<T>?

    companion object {
        private val camelRegex = Regex("([A-Z])")
        fun String.fromCamelCaseToSnakeCase(): String {
            return this.replace(camelRegex, "_$1").removePrefix("_").lowercase()
        }
        fun String.hasNamespace(): Boolean = contains(":")
    }
}

