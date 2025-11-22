package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.fromCamelCaseToSnakeCase
import com.mineinabyss.geary.serialization.ComponentSerializers.Companion.hasNamespace
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.capturedKClass
import kotlin.reflect.KClass

class SerializersByMap : ComponentSerializers {
    private val serialName2Component = mutableMapOf<String, KClass<out Component>>()
    val component2serialName = mutableMapOf<KClass<out Component>, String>()

    override fun <T : Any> registerSerialNameFor(kClass: KClass<T>, serialName: String) {
        serialName2Component[serialName] = kClass
        component2serialName[kClass] = serialName
    }


    //TODO allow this to work for all registered classes, not just components
    override fun getClassFor(serialName: String, namespaces: List<String>): KClass<out Component> {
        val defaultNamespaces = (namespaces + "geary").toSet()
        val parsedKey = serialName.fromCamelCaseToSnakeCase()
        return (if (parsedKey.hasNamespace())
            serialName2Component[parsedKey]
        else defaultNamespaces.firstNotNullOfOrNull { namespace ->
            serialName2Component["$namespace:$parsedKey"]
        })
            ?: error("$parsedKey is not a component registered in any of the namespaces: $defaultNamespaces")
    }

    override fun getSerialNameFor(kClass: KClass<out Component>): String? =
        component2serialName[kClass]

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getKClassFor(serializer: KSerializer<T>): KClass<T>? {
        // If this is a contextual serializer, we already have the type information
        serializer.descriptor.capturedKClass?.let { return it as KClass<T> }

        // Otherwise, look it up by serialName
        return serialName2Component[serializer.descriptor.serialName] as KClass<T>?
    }
}
