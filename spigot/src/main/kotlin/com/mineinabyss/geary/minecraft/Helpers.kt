package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.components.PrefabKey
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.minecraft.config.GearyConfig
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.idofront.messaging.broadcast
import net.minecraft.server.v1_16_R2.EntityTypes
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin


/** Gets [GearyPlugin] via Bukkit once, then sends that reference back afterwards */
public val geary: GearyPlugin by lazy { JavaPlugin.getPlugin(GearyPlugin::class.java) }

internal fun debug(message: Any?) {
    if (GearyConfig.data.debug) broadcast(message)
}

/** Verifies a [PersistentDataContainer] has a tag identifying it as containing Geary components. */
public var PersistentDataContainer.hasComponentsEncoded: Boolean
    get() = has(SpigotEngine.componentsKey, PersistentDataType.BYTE)
    set(value) {
        when {
            //TODO are there any empty marker keys?
            value -> if (!hasComponentsEncoded) set(SpigotEngine.componentsKey, PersistentDataType.BYTE, 1)
            else -> remove(SpigotEngine.componentsKey)
        }
    }

public fun Location.spawnGeary(prefab: PrefabKey): Entity? {
    val type = PrefabManager[prefab]?.get<EntityTypes<*>>()
    return TODO()
}
