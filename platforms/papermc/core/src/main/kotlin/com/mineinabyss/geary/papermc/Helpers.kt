package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.events.GearyAttemptMinecraftSpawnEvent
import com.mineinabyss.geary.papermc.events.GearyMinecraftSpawnEvent
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.helpers.addPrefab
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal fun KoinComponent.debug(message: Any?) {
    if (get<GearyConfig>().debug) broadcast(message)
}

/** Verifies a [PersistentDataContainer] has a tag identifying it as containing Geary components. */
public var PersistentDataContainer.hasComponentsEncoded: Boolean
    get() = GearyMCKoinComponent { has(engine.componentsKey, PersistentDataType.BYTE) }
    set(value) = GearyMCKoinComponent {
        when {
            //TODO are there any empty marker keys?
            value -> if (!hasComponentsEncoded) set(engine.componentsKey, PersistentDataType.BYTE, 1)
            else -> remove(engine.componentsKey)
        }
    }

public fun Location.spawnFromPrefab(prefab: PrefabKey): Entity? = GearyMCKoinComponent {
    return spawnFromPrefab(prefabManager[prefab] ?: return null)
}

public fun Location.spawnFromPrefab(prefab: GearyEntity): Entity? {
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
