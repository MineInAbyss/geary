package com.mineinabyss.geary.serialization.dsl

import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.modules.GearyConfiguration
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.formats.Format
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
    builder: SerializableComponents.Builder
) {
    val serializers = builder.serializersBuilder
    val formats = builder.formatsBuilder

    /** Adds a [SerializersModule] for polymorphic serialization of [Component]s within the ECS. */
    inline fun components(crossinline init: PolymorphicModuleBuilder<Component>.() -> Unit) {
        module { polymorphic(Component::class) { init() } }
    }

    fun format(ext: String, format: (SerializersModule) -> Format) {
        formats.register(ext, format)
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
        if (serializers.serialNameToClass.containsKey(serialName)) {
            error("A component with serial name $serialName is already registered")
        }
        serializers.serialNameToClass[serialName] = subclass
        subclass(subclass, serializer)
    }

    inline fun <reified T : Any> namedComponent(name: String) {
        serializers.serialNameToClass[name] = T::class
    }

    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
    inline fun module(init: SerializersModuleBuilder.() -> Unit) {
        serializers.modules += SerializersModule { init() }
    }
}

@GearyDSL
fun GearyConfiguration.serialization(configure: SerializableComponentsDSL.() -> Unit) =
    install(SerializableComponents).also { SerializableComponentsDSL(it).configure() }
