package com.mineinabyss.geary.dsl

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.geary.ecs.types.EntityTypeManager
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.ecs.types.GearyEntityTypes
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

private val registeredExtensions = mutableMapOf<Plugin, GearyExtension>()

//TODO make a reusable solution for extensions within idofront
public class GearyExtension(
        plugin: Plugin,
        types: GearyEntityTypes<*>?,
) {
    init {
        if (types != null)
            EntityTypeManager.add(plugin, types)
    }

    public fun systems(vararg systems: TickingSystem) {
        Engine.addSystems(*systems)
    }

    public inline fun <reified T : GearyComponent> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
        Formats.addSerialName(serializer.descriptor.serialName, T::class)
        subclass(T::class, serializer)
    }

    public inline fun components(crossinline init: PolymorphicModuleBuilder<GearyComponent>.() -> Unit) {
        serializers { polymorphic(GearyComponent::class) { init() } }
    }

    public fun serializers(init: SerializersModuleBuilder.() -> Unit) {
        Formats.addSerializerModule(SerializersModule { init() })
    }

    public fun bukkitEntityAccess(init: BukkitEntityAccessExtension.() -> Unit) {
        BukkitEntityAccessExtension().apply(init)
    }

    public class BukkitEntityAccessExtension {
        public fun onPlayerRegister(list: MutableList<GearyComponent>.(Player) -> Unit) {
            BukkitEntityAccess.playerRegistryExtensions += list
        }

        public fun onPlayerUnregister(run: (GearyEntity, Player) -> Unit) {
            BukkitEntityAccess.playerUnregisterExtensions += run
        }

        public fun entityConversion(getter: Entity.() -> GearyEntity?) {
            BukkitEntityAccess.bukkitEntityAccessExtensions += getter
        }
    }

}

public inline fun <reified T : GearyEntityType> Plugin.attachToGeary(
        types: GearyEntityTypes<T>? = null,
        init: GearyExtension.() -> Unit) {
    //TODO support plugins being re-registered after a reload
    GearyExtension(this, types).apply(init).apply {
        components {
            // Whenever we're using this serial module to deserialize our components we want to access them by
            // reference through geary, not by using the actual EntityType's serializer like we would when
            // reading config files.
            component(GearyEntityType.ByReferenceSerializer(T::class))
        }
    }
}
