package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

interface ComponentSerializers {
    val module: SerializersModule

    //TODO allow this to work for all registered classes, not just components
    fun getClassFor(serialName: String): KClass<out Component>

    fun <T : Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>,
        namespaces: List<String> = emptyList()
    ): DeserializationStrategy<T>?

    fun <T : Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<out T>?
    fun getSerialNameFor(kClass: KClass<out Component>): String?
}

