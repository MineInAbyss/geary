package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.components.addComponents
import com.mineinabyss.geary.minecraft.config.GearyConfig
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.events.GearyMinecraftSpawnEvent
import com.mineinabyss.geary.minecraft.store.geary
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.spawning.spawn
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin


/** Gets [GearyPlugin] via Bukkit once, then sends that reference back afterwards */
public val geary: GearyPlugin by lazy { JavaPlugin.getPlugin(GearyPlugin::class.java) }

internal fun debug(message: Any?) {
    if (GearyConfig.data.debug) broadcast(message)
}

/** Verifies a [PersistentDataContainer] has a tag identifying it as containing Geary components. */
public var PersistentDataContainer.isGearyEntity: Boolean
    get() = has(SpigotEngine.componentsKey, PersistentDataType.BYTE)
    set(value) =
        when {
            value -> set(SpigotEngine.componentsKey, PersistentDataType.BYTE, 1) //TODO are there any empty keys?
            else -> remove(SpigotEngine.componentsKey)
        }

/** Spawns a bukkit entity of a certain [type] and attaches it to a geary entity with [components]. */
public fun Location.spawnGeary(type: EntityType, components: List<GearyComponent>): Entity? {
    return spawn(type) {
        if (components.isNotEmpty())
            geary(this) {
                addComponents(components)
                GearyMinecraftSpawnEvent(this).call()
            }
    }
}
