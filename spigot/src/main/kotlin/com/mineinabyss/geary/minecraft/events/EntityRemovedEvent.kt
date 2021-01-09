package com.mineinabyss.geary.minecraft.events

import com.mineinabyss.geary.ecs.GearyEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList


/**
 * Called whenever an entity is asked to be removed from the ECS, before its components are actually cleared.
 */
//TODO Ensure calling Bukkit events during system iteration isn't going to cause any issues
public data class EntityRemovedEvent(
        val entity: GearyEntity
): Event() {
    override fun getHandlers(): HandlerList = handlerList

    public companion object {
        @JvmStatic
        public val handlerList: HandlerList = HandlerList()
    }
}
