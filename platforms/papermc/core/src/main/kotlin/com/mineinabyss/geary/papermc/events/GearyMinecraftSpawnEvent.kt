package com.mineinabyss.geary.papermc.events

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a Geary entity with a linked Bukkit representation is spawned into the world, after components
 * are attached.
 *
 * This does not get called when an existing Bukkit entity gets a representation in Geary.
 */
public data class GearyMinecraftSpawnEvent(
    val entity: GearyEntity
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    internal companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
