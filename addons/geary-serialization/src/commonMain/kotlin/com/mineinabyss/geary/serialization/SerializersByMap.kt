package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.hasNamespace
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

class SerializersByMap(
    override val module: SerializersModule,
    val serialName2Component: Map<String, KClass<out Component>>
) : ComponentSerializers {
    val component2serialName: Map<KClass<out Component>, String> = serialName2Component
        .entries
        .associate { it.value to it.key }

    //TODO allow this to work for all registered classes, not just components
    override fun getClassFor(serialName: String, namespaces: List<String>): KClass<out Component> {
        val parsedKey = serialName.fromCamelCaseToSnakeCase()
        return (if (parsedKey.hasNamespace())
            serialName2Component[parsedKey]
        else namespaces.firstNotNullOfOrNull { namespace ->
            serialName2Component["$namespace:$parsedKey"]
        })
            ?: error("$parsedKey is not a component registered in any of the namespaces: $namespaces")
    }

    override fun <T : Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>,
    ): DeserializationStrategy<T>? =
        module.getPolymorphic(baseClass = baseClass, serializedClassName = key)

    override fun <T : Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<out T>? {
        val serialName = getSerialNameFor(kClass) ?: return null

        return getSerializerFor(serialName, Component::class)
    }

    override fun getSerialNameFor(kClass: KClass<out Component>): String? =
        component2serialName[kClass]

}
