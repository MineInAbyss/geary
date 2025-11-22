package com.mineinabyss.geary.serialization

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.components.Persists
import com.mineinabyss.geary.serialization.formats.Format
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass

//data class ComponentRegisterResult(
//    val success: Map<KClass<*>, KSerializer<*>>,
//    val failure: Map<KClass<*>, Throwable>,
//)

data class SerializableComponentsModule(
    val serializers: ComponentSerializers,
    val formats: SerializationFormats,
    val logger: Logger,
    val world: Geary,
) {
    val persists: ComponentId = world.componentId<Persists>()

    inline fun <reified T : Any> namedComponent(name: String) {
        serializers.registerSerialNameFor(T::class, name)
    }

    @OptIn(InternalSerializationApi::class)
    fun registerComponentSerializers(components: Collection<KClass<*>>) {
        val associated = components.mapNotNull { component ->
            runCatching {
                component.serializerOrNull()
            }.onFailure {
                when {
                    logger.config.minSeverity <= Severity.Verbose -> logger.w("Failed to register component ${component.simpleName}\n${it.stackTraceToString()}")
                    else -> logger.w("Failed to register component ${component.simpleName} ${it::class.simpleName}: ${it.message}")
                }
            }.getOrNull()?.let { component to it }
        }
        registerComponentSerializers(associated.toMap())
    }

    fun registerComponentSerializers(components: Map<KClass<*>, KSerializer<*>>) {
        formats.addModule {
            polymorphic(Any::class) {
                components.forEach { (kClass, serializer) ->
                    subclass(kClass as KClass<Any>, serializer as KSerializer<Any>)
                }
            }
        }
        components.forEach { (kClass, serializer) ->
            serializers.registerSerialNameFor(kClass, serializer.descriptor.serialName)
        }
    }

    fun registerComponentSerializers(vararg pairs: Pair<KClass<*>, KSerializer<*>>) {
        registerComponentSerializers(pairs.toMap())
    }

    fun format(key: String, format: (SerializersModule) -> Format) {
        formats.registerFormat(key, format)
    }
}