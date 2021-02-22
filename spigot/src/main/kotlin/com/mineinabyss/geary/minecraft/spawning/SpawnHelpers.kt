package com.mineinabyss.geary.minecraft.spawning

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.GearyPrefab
import com.mineinabyss.geary.minecraft.events.GearyMinecraftSpawnEvent
import com.mineinabyss.geary.minecraft.store.*
import com.mineinabyss.idofront.events.call
import com.mineinabyss.idofront.spawning.spawn
import org.bukkit.Location
import org.bukkit.entity.EntityType

/** Spawns a bukkit entity of a certain [type] and attaches it to a geary entity with [components]. */
public fun Location.spawnGeary(type: EntityType, prefab: GearyPrefab? = null): GearyEntity? {
    val bukkit = spawn(type) ?: return null
    return geary(bukkit) {
        prefab?.decodeComponentsTo(this)
        GearyMinecraftSpawnEvent(this).call()
    }
}
