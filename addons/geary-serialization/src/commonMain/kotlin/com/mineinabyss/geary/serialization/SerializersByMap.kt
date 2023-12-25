package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
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
    override fun getClassFor(serialName: String): KClass<out Component> =
        serialName2Component[serialName]
            ?: error("$serialName is not a valid component name in the registered components")

    override fun <T : Component> getSerializerFor(
        key: String,
        baseClass: KClass<in T>,
        namespaces: List<String>,
    ): DeserializationStrategy<T>? =
        if (key.hasNamespace())
            module.getPolymorphic(baseClass = baseClass, serializedClassName = key)
        else {
            namespaces.firstNotNullOfOrNull { namespace ->
                module.getPolymorphic(baseClass = baseClass, serializedClassName = "$namespace:$key")
            } ?: error("No serializer found for $key in any of the namespaces $namespaces")
        }

    override fun <T : Component> getSerializerFor(kClass: KClass<in T>): DeserializationStrategy<out T>? {
        val serialName = getSerialNameFor(kClass) ?: return null

        return getSerializerFor(serialName, Component::class)
    }

    override fun getSerialNameFor(kClass: KClass<out Component>): String? =
        component2serialName[kClass]

    private fun String.hasNamespace(): Boolean = contains(":")
    private fun String.prefixNamespaceIfNotPrefixed(): String =
        if (!hasNamespace())
            "geary:${this}"
        else this
}
