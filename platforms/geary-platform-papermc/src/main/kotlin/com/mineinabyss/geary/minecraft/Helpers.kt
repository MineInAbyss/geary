package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.addPrefab
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.geary.minecraft.config.GearyConfig
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.events.GearyAttemptMinecraftSpawnEvent
import com.mineinabyss.geary.minecraft.events.GearyMinecraftSpawnEvent
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

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
    val attemptSpawn = GearyAttemptMinecraftSpawnEvent(this, prefab)
    attemptSpawn.call()
    val bukkitEntity = attemptSpawn.bukkitEntity ?: return null

    bukkitEntity.toGeary {
        addPrefab(prefab)
        GearyMinecraftSpawnEvent(this).call()
    }

    return bukkitEntity
}

public fun NamespacedKey.toPrefabKey(): PrefabKey = PrefabKey.of(namespace, key)
