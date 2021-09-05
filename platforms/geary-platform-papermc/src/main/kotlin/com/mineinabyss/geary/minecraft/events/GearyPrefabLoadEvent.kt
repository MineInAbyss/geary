package com.mineinabyss.geary.minecraft.events

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

public data class GearyPrefabLoadEvent(
    val entity: GearyEntity
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    internal companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
