package com.mineinabyss.geary.dsl

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.systems.TickingSystem
import com.mineinabyss.geary.ecs.types.EntityTypeManager
import com.mineinabyss.geary.ecs.types.GearyEntityTypes
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

private val registeredExtensions = mutableMapOf<Plugin, GearyExtension>()

//TODO make a reusable solution for extensions within idofront
public class GearyExtension(
        internal val plugin: Plugin,
        internal val types: GearyEntityTypes<*>,
) {
    init {
        EntityTypeManager.add(plugin, types)
    }

    public fun systems(vararg systems: TickingSystem) {
        Engine.addSystems(*systems)
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

        public fun onPlayerUnregister(run: (GearyEntity, Player) -> Unit){
            BukkitEntityAccess.playerUnregisterExtensions += run
        }

        public fun entityConversion(getter: Entity.() -> GearyEntity?){
            BukkitEntityAccess.bukkitEntityAccessExtensions += getter
        }
    }
}

public fun Plugin.attachToGeary(
        types: GearyEntityTypes<*>,
        init: GearyExtension.() -> Unit) {
    //TODO support plugins being re-registered after a reload
    GearyExtension(this, types).apply(init)
}
