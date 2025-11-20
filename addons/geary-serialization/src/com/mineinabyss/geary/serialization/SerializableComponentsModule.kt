package com.mineinabyss.geary.serialization

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.serialization.formats.Format
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

data class ComponentRegisterResult(
    val success: Map<KClass<*>, KSerializer<*>>,
    val failure: Map<KClass<*>, Throwable>,
)

data class SerializableComponentsModule(
    val serializers: ComponentSerializers,
    val formats: SerializationFormats,
    val logger: Logger,
    val persists: ComponentId,
) {
    inline fun <reified T : Any> namedComponent(name: String) {
        TODO()
//        serializers.serialNameToClass[name] = T::class
    }

    fun registerComponentSerializers(vararg serializers: KSerializer<*>): ComponentRegisterResult {
        TODO()
    }

    fun registerComponentSerializers(components: Collection<KClass<*>>): ComponentRegisterResult {

//        when {
//            logger.config.minSeverity <= Severity.Verbose -> logger.w("Failed to register component ${scannedComponent.simpleName}\n${it.stackTraceToString()}")
//            else -> logger.w("Failed to register component ${scannedComponent.simpleName} ${it::class.simpleName}: ${it.message}")
//        }
        TODO()
    }

    fun registerComponentSerializers(components: Map<KClass<*>, KSerializer<*>>): ComponentRegisterResult {
        TODO()
    }

    fun registerComponentSerializers(vararg pairs: Pair<KClass<*>, KSerializer<*>>): ComponentRegisterResult {
        TODO()
    }

    fun format(key: String, format: (SerializersModule) -> Format) {
        formats.registerFormat(key, format)
    }
}