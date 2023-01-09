package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.events.GearyAttemptMinecraftSpawnEvent
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.events.call
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity

fun Location.spawnFromPrefab(prefab: PrefabKey): Entity? {
    val entity = prefabs.manager[prefab] ?: return null
    return spawnFromPrefab(entity)
}

fun Location.spawnFromPrefab(prefab: GearyEntity): Entity? {
    val attemptSpawn = GearyAttemptMinecraftSpawnEvent(this, prefab)
    attemptSpawn.call()

    return attemptSpawn.bukkitEntity
}

fun NamespacedKey.toPrefabKey(): PrefabKey = PrefabKey.of(namespace, key)
fun PrefabKey.toNamespacedKey(): NamespacedKey = NamespacedKey(namespace, key)
