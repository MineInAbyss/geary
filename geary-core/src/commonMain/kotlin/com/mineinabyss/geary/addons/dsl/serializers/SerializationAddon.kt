package com.mineinabyss.geary.addons.dsl.serializers

import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyDSLMarker
import com.mineinabyss.geary.addons.dsl.Namespaced
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.modules.GearyModule
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

class SerializationAddon {
    val serializers get() = geary.serializers

    /** Adds a [SerializersModule] for polymorphic serialization of [Component]s within the ECS. */
    inline fun Namespaced.components(crossinline init: PolymorphicModuleBuilder<Component>.() -> Unit) {
        module { polymorphic(Component::class) { init() } }
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
    fun <T : Component> PolymorphicModuleBuilder<T>.component(
        kClass: KClass<T>,
        serializer: KSerializer<T>?
    ): Boolean {
        val serialName = serializer?.descriptor?.serialName ?: return false
        if (!serializers.isRegistered(serialName)) {
            serializers.registerSerialName(serialName, kClass)
            subclass(kClass, serializer)
            return true
        }
        return false
    }

    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
    inline fun Namespaced.module(init: SerializersModuleBuilder.() -> Unit) {
        serializers.addSerializersModule(namespace, SerializersModule { init() })
    }

    companion object Plugin: GearyAddon<SerializationAddon> {
        override fun install(geary: GearyModule): SerializationAddon {
            TODO("Not yet implemented")
        }

    }
}

@GearyDSLMarker
fun GearyModule.serialization(configure: SerializationAddon.() -> Unit) {
    addons.getOrNull<SerializationAddon>()?.configure() ?: install(SerializationAddon, configure)
}
