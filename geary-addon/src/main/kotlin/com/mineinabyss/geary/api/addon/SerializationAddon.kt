package com.mineinabyss.geary.api.addon

import com.mineinabyss.geary.context.GearyContext
import com.mineinabyss.geary.context.GearyContextKoin
import com.mineinabyss.geary.datatypes.GearyComponent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

public class SerializationAddon(
    @PublishedApi
    internal val addon: GearyAddon,
) : GearyContext by GearyContextKoin() {
    /** Adds a [SerializersModule] for polymorphic serialization of [GearyComponent]s within the ECS. */
    public inline fun components(crossinline init: PolymorphicModuleBuilder<GearyComponent>.() -> Unit) {
        module { polymorphic(GearyComponent::class) { init() } }
    }


    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    public inline fun <reified T : GearyComponent> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
        component(T::class, serializer)
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    public fun <T : GearyComponent> PolymorphicModuleBuilder<T>.component(
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
    public inline fun module(init: SerializersModuleBuilder.() -> Unit) {
        serializers.addSerializersModule(addon.namespace, SerializersModule { init() })
    }
}

public fun GearyAddon.serialization(init: SerializationAddon.() -> Unit) {
    startup {
        GearyLoadPhase.REGISTER_SERIALIZERS {
            SerializationAddon(this@serialization).init()
        }
    }
}
