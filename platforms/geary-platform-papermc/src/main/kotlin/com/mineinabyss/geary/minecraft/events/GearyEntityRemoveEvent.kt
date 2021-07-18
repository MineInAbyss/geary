package com.mineinabyss.geary.minecraft.events

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList


/**
 * Called whenever an entity with a related Bukkit Entity is asked to be removed from the ECS,
 * before its components are actually cleared.
 */
//TODO maybe a more descriptive title and better documentation
//TODO Ensure calling Bukkit events during system iteration isn't going to cause any issues
public data class GearyEntityRemoveEvent(
        val entity: GearyEntity
): Event() {
    override fun getHandlers(): HandlerList = handlerList

    public companion object {
        @JvmStatic
        public val handlerList: HandlerList = HandlerList()
    }
}
