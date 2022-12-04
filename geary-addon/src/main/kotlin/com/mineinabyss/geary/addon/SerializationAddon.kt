package com.mineinabyss.geary.addon

import com.mineinabyss.geary.addon.GearyLoadPhase.REGISTER_SERIALIZERS
import com.mineinabyss.geary.context.GearyModule
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

class SerializationAddon(
    @PublishedApi
    internal val addon: GearyAddon,
) : GearyModule by GearyContextKoin() {
    /** Adds a [SerializersModule] for polymorphic serialization of [Component]s within the ECS. */
    inline fun components(crossinline init: PolymorphicModuleBuilder<Component>.() -> Unit) {
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
    inline fun module(init: SerializersModuleBuilder.() -> Unit) {
        serializers.addSerializersModule(addon.namespace, SerializersModule { init() })
    }
}

fun GearyAddon.serialization(init: SerializationAddon.() -> Unit) {
    startup {
        REGISTER_SERIALIZERS {
            SerializationAddon(this@serialization).init()
        }
    }
}
