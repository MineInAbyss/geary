package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.PrefabKey
import com.mineinabyss.geary.ecs.entities.addPrefab
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.geary.minecraft.components.BukkitEntityType
import com.mineinabyss.geary.minecraft.config.GearyConfig
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.events.GearyMinecraftSpawnEvent
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcast
import com.mineinabyss.idofront.nms.aliases.NMSEntityType
import com.mineinabyss.idofront.nms.spawnEntity
import com.mineinabyss.idofront.spawning.spawn
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.entity.CreatureSpawnEvent
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
    return spawnGeary(PrefabManager[prefab] ?: return null)
}

public fun Location.spawnGeary(prefab: GearyEntity): Entity? {
    val entity = prefab.get<NMSEntityType<*>>()?.let { type ->
        spawnEntity(type, spawnReason = CreatureSpawnEvent.SpawnReason.CUSTOM)
    } ?: prefab.get<BukkitEntityType>()?.type?.let { type ->
        spawn(type)
    } ?: return null

    geary(entity) {
        addPrefab(prefab)
        GearyMinecraftSpawnEvent(this).call()
    }
    return entity
}
