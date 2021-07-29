package com.mineinabyss.geary.minecraft.events

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called whenever an existing Geary entity with a linked Bukkit representation starts to get loaded, before any
 * components are attached.
 */
public data class GearyMinecraftPreLoadEvent(
    val entity: GearyEntity,
    val bukkitEntity: BukkitEntity,
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    internal companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
