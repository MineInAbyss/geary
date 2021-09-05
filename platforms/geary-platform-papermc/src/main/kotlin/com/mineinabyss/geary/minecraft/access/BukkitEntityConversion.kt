package com.mineinabyss.geary.minecraft.access

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.Entity

public fun Entity.toGeary(): GearyEntity =
    BukkitAssociations.get(uniqueId) ?: BukkitEntityAssociations.registerEntity(this)

// Separate function because inline `run` cannot be nullable
//TODO we want to call load entity event after init runs
public inline fun Entity.toGeary(init: GearyEntity.() -> Unit): GearyEntity =
    toGeary().apply { init() }

public fun Entity.toGearyOrNull(): GearyEntity? = BukkitAssociations[uniqueId]

public fun GearyEntity.toBukkit(): Entity? = get<Entity>()
