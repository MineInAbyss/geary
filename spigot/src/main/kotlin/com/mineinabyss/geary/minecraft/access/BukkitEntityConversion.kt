package com.mineinabyss.geary.minecraft.access

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.events.GearyMinecraftLoadEvent
import com.mineinabyss.idofront.events.call
import org.bukkit.entity.Entity

public fun geary(entity: Entity): GearyEntity = BukkitEntityAccess.getEntity(entity).apply {
    GearyMinecraftLoadEvent(this).call()
}

// Separate function because inline `run` cannot be nullable
public inline fun geary(entity: Entity, init: GearyEntity.() -> Unit): GearyEntity = geary(entity).apply {
    init()
    // We want this to run after potential spawn events defined in init
    GearyMinecraftLoadEvent(this).call()
}

public fun gearyOrNull(entity: Entity): GearyEntity? = BukkitEntityAccess.getEntityOrNull(entity)

public fun GearyEntity.toBukkit(): Entity? = get<Entity>()
