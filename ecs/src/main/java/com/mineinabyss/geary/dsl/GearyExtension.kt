package com.mineinabyss.geary.dsl

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.geary.ecs.types.EntityTypeManager
import com.mineinabyss.geary.ecs.types.GearyEntityType
import com.mineinabyss.geary.ecs.types.GearyEntityTypes
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

//TODO make a reusable solution for extensions within idofront
/**
 * The entry point for other plugins to hook into Geary. Allows registering serializable components, systems, actions,
 * and more.
 */
public class GearyExtension(
        plugin: Plugin,
        types: GearyEntityTypes<*>?,
) {
    init {
        if (types != null)
            EntityTypeManager.add(plugin, types)
    }

    /** Registers a list of [systems]. */
    public fun systems(vararg systems: TickingSystem) {
        Engine.addSystems(*systems)
    }

    /**
     * Adds a serializable component and registers it with Geary to allow finding the appropriate class via
     * component serial name.
     */
    public inline fun <reified T : GearyComponent> PolymorphicModuleBuilder<T>.component(serializer: KSerializer<T>) {
        val name = serializer.descriptor.serialName
        if(name !in Formats.componentSerialNames) {
            Formats.addSerialName(name, T::class)
            subclass(T::class, serializer)
        }
    }

    /** Adds a [SerializersModule] for polymorphic serialization of [GearyComponent]s within the ECS. */
    public inline fun components(crossinline init: PolymorphicModuleBuilder<GearyComponent>.() -> Unit) {
        serializers { polymorphic(GearyComponent::class) { init() } }
    }

    /** Adds a [SerializersModule] for polymorphic serialization of [GearyAction]s within the ECS. */
    public inline fun actions(crossinline init: PolymorphicModuleBuilder<GearyAction>.() -> Unit) {
        serializers { polymorphic(GearyAction::class) { init() } }
    }

    /** Adds a serializable action. */
    public inline fun <reified T : GearyAction> PolymorphicModuleBuilder<T>.action(serializer: KSerializer<T>) {
        subclass(serializer)
    }

    /** Adds a [SerializersModule] to be used for polymorphic serialization within the ECS. */
    public fun serializers(init: SerializersModuleBuilder.() -> Unit) {
        Formats.addSerializerModule(SerializersModule { init() })
    }

    /** Entry point for extending behaviour regarding how bukkit entities are linked to the ECS. */
    public fun bukkitEntityAccess(init: BukkitEntityAccessExtension.() -> Unit) {
        BukkitEntityAccessExtension().apply(init)
    }

    /** Entry point for extending behaviour regarding how bukkit entities are linked to the ECS. */
    public class BukkitEntityAccessExtension {
        /** Additional components to be added to the player when they are registered with the ECS. */
        public fun onPlayerRegister(list: MutableList<GearyComponent>.(Player) -> Unit) {
            BukkitEntityAccess.playerRegistryExtensions += list
        }

        /** Additional things to do before a player's [GearyEntity] is removed. */
        public fun onPlayerUnregister(run: (GearyEntity, Player) -> Unit) {
            BukkitEntityAccess.playerUnregisterExtensions += run
        }

        /**
         * Additional ways of getting a [GearyEntity] given a spigot [Entity]. Will try one by one until a conversion
         * is not null. There is currently no priority system.
         */
        //TODO priority system
        public fun entityConversion(getter: Entity.() -> GearyEntity?) {
            BukkitEntityAccess.bukkitEntityAccessExtensions += getter
        }
    }

}

/**
 * Entry point to register a new [Plugin] with the Geary ECS.
 *
 * @param types The subclass of [GearyEntityTypes] associated with this plugin.
 */
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
