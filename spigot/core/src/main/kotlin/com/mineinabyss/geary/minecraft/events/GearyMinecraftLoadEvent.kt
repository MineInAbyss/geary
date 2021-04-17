package com.mineinabyss.geary.minecraft.events

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called whenever an existing Geary entity with a linked Bukkit representation gets loaded after components are
 * attached. For instance, when a chunk gets loaded with this entity inside.
 *
 * This does not get called when an existing Bukkit entity gets a representation in Geary.
 *
 * Unlike [GearyMinecraftSpawnEvent] this keeps getting called upon subsequent loads of the entity, not just the first
 * creation.
 */
//TODO implement this event for more than Mobzy mobs.
public data class GearyMinecraftLoadEvent(
    val entity: GearyEntity
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    internal companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
