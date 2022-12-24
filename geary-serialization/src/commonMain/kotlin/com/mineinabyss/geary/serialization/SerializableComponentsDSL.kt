package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass

@GearyDSL
class SerializableComponentsDSL(
    val namespaced: Namespaced
) {
    val serializers = serializableComponents.serializers

    /** Adds a [SerializersModule] for polymorphic serialization of [Component]s within the ECS. */
    inline fun components(crossinline init: PolymorphicModuleBuilder<Component>.() -> Unit) {
        module { polymorphic(Component::class) { init() } }
    }

    fun format(format: (SerializersModule) -> Format) {
        serializableComponents.formats.register(ext, format)
    }

    fun format(ext: String, format: (SerializersModule) -> Format) {
        serializableComponents.formats.register(ext, format)
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    inline fun <reified T : Component> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
        component(T::class, serializer)
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    @OptIn(InternalSerializationApi::class)
    fun <T : Component> PolymorphicModuleBuilder<T>.component(
        subclass: KClass<T>,
        serializer: KSerializer<T> = subclass.serializerOrNull()
            ?: error("No serializer found for $subclass while registering serializable component")
    ) {
        val serialName = serializer.descriptor.serialName
        if (!serializers.isRegistered(serialName)) {
            serializers.registerSerialName(serialName, subclass)
            subclass(subclass, serializer)
        }
    }

    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
    inline fun module(init: SerializersModuleBuilder.() -> Unit) {
        serializers.addSerializersModule(namespaced.namespace, SerializersModule { init() })
    }
}

@GearyDSL
fun Namespaced.serialization(configure: SerializableComponentsDSL.() -> Unit) =
    gearyConf.install(SerializableComponents).also { SerializableComponentsDSL(this).configure() }
