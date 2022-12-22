package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

interface Serializers {
    //TODO allow this to work for all registered classes, not just components
    fun getClassFor(serialName: String): KClass<out Component>
    fun isRegistered(serialName: String): Boolean

    /**
     * Adds a class associated with a serial name. Currently haven't found an easy way to get this using serializer
     * modules, but if possible this will be removed.
     */
    fun registerSerialName(name: String, kClass: KClass<out Component>)
    fun <T : Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>
    ): DeserializationStrategy<out T>?

    fun <T : Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<out T>?
    fun getSerialNameFor(kClass: KClass<out Component>): String?
    fun addSerializersModule(namespace: String, module: SerializersModule)
    fun clearSerializerModule(addonName: String)
}
