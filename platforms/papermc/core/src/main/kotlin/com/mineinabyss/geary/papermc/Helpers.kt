package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.events.GearyAttemptMinecraftSpawnEvent
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal fun KoinComponent.debug(message: Any?) {
    if (get<GearyConfig>().debug) broadcast(message)
}

public fun Location.spawnFromPrefab(prefab: PrefabKey): Entity? {
    val entity = globalContextMC.prefabManager[prefab] ?: return null
    return spawnFromPrefab(entity)
}

public fun Location.spawnFromPrefab(prefab: GearyEntity): Entity? {
    val attemptSpawn = GearyAttemptMinecraftSpawnEvent(this, prefab)
    attemptSpawn.call()
    val bukkitEntity = attemptSpawn.bukkitEntity ?: return null

    return bukkitEntity
}

public fun NamespacedKey.toPrefabKey(): PrefabKey = PrefabKey.of(namespace, key)
public fun PrefabKey.toNamespacedKey(): NamespacedKey = NamespacedKey(namespace, name)
