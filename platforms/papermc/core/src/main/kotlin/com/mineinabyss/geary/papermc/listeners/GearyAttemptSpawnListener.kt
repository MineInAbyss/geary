package com.mineinabyss.geary.papermc.listeners

import com.mineinabyss.geary.papermc.components.BukkitEntityType
import com.mineinabyss.geary.papermc.events.GearyAttemptMinecraftSpawnEvent
import com.mineinabyss.idofront.spawning.spawn
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

public class GearyAttemptSpawnListener : Listener {
    @EventHandler
    public fun GearyAttemptMinecraftSpawnEvent.readBukkitEntityType() {
        if (bukkitEntity == null) runBlocking {
            bukkitEntity = prefab.get<BukkitEntityType>()?.type?.let { location.spawn(it) }
        }
    }
}
