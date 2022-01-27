package com.mineinabyss.geary.papermc.listeners

import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.papermc.components.BukkitEntityType
import com.mineinabyss.geary.papermc.events.GearyAttemptMinecraftSpawnEvent
import com.mineinabyss.idofront.spawning.spawn
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

public object GearyAttemptSpawnListener : Listener, GearyListener() {
    //    val TargetScope.entityType
    @EventHandler
    public fun GearyAttemptMinecraftSpawnEvent.readBukkitEntityType() {
        if (bukkitEntity == null)
            bukkitEntity = prefab.get<BukkitEntityType>()?.type?.let { location.spawn(it) }
    }
}
