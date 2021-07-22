package com.mineinabyss.geary.minecraft.access

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.Entity

public fun geary(entity: Entity): GearyEntity =
    BukkitAssociations.get(entity.uniqueId) ?: BukkitEntityAssociations.registerEntity(entity)

// Separate function because inline `run` cannot be nullable
//TODO we want to call load entity event after init runs
public inline fun geary(entity: Entity, init: GearyEntity.() -> Unit): GearyEntity =
    geary(entity).apply { init() }

public fun gearyOrNull(entity: Entity): GearyEntity? = BukkitAssociations.get(entity.uniqueId)

public fun GearyEntity.toBukkit(): Entity? = get<Entity>()
