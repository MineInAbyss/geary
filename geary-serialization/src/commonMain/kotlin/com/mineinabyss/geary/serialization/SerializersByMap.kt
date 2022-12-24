package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import kotlin.reflect.KClass

class SerializersByMap : ComponentSerializers {
    private val addonToModuleMap = mutableMapOf<String, SerializersModule>()
    private val serialName2Component: MutableMap<String, KClass<out Component>> = mutableMapOf()
    private val component2serialName: MutableMap<KClass<out Component>, String> = mutableMapOf()
    val module: SerializersModule by lazy {
        addonToModuleMap.values.fold(EmptySerializersModule()) { acc, module ->
            acc.overwriteWith(module)
        }
    }

    //TODO allow this to work for all registered classes, not just components
    override fun getClassFor(serialName: String): KClass<out Component> =
        serialName2Component[serialName]
            ?: error("$serialName is not a valid component name in the registered components")

    override fun isRegistered(serialName: String): Boolean =
        serialName in serialName2Component

    override fun registerSerialName(name: String, kClass: KClass<out Component>) {
        serialName2Component[name] = kClass
        component2serialName[kClass] = name
    }

    override fun <T: Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>
    ): DeserializationStrategy<out T>? =
        module.getPolymorphic(baseClass = baseClass, serializedClassName = key)

    override fun <T: Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<out T>? {
        val serialName = getSerialNameFor(kClass) ?: return null

        return getSerializerFor(serialName, Component::class)
    }

    override fun getSerialNameFor(kClass: KClass<out Component>): String? =
        component2serialName[kClass]

    override fun addSerializersModule(namespace: String, module: SerializersModule) {
        addonToModuleMap[namespace] =
            addonToModuleMap.getOrElse(namespace) { EmptySerializersModule() }.overwriteWith(module)
    }

    override fun clearSerializerModule(addonName: String) {
        addonToModuleMap -= addonName
    }
}
