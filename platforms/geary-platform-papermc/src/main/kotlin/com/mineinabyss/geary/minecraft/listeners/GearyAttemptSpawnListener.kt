package com.mineinabyss.geary.minecraft.listeners

import com.mineinabyss.geary.minecraft.components.BukkitEntityType
import com.mineinabyss.geary.minecraft.events.GearyAttemptMinecraftSpawnEvent
import com.mineinabyss.idofront.spawning.spawn
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

public object GearyAttemptSpawnListener: Listener {
    @EventHandler
    public fun GearyAttemptMinecraftSpawnEvent.readBukkitEntityType() {
        if (bukkitEntity == null)
            bukkitEntity = prefab.get<BukkitEntityType>()?.type?.let { location.spawn(it) }
    }
}
