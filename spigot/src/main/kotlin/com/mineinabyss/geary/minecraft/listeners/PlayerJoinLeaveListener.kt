package com.mineinabyss.geary.minecraft.listeners

import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import com.mineinabyss.idofront.destructure.component1
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

public object PlayerJoinLeaveListener : Listener {
    //TODO fire on reload
    @EventHandler
    public fun onPlayerLogin(e: PlayerLoginEvent) {
        val (player) = e
        BukkitEntityAccess.registerPlayer(player)
    }

    @EventHandler
    public fun onPlayerQuit(e: PlayerQuitEvent) {
        val (player) = e
        BukkitEntityAccess.unregisterPlayer(player)
    }
}
