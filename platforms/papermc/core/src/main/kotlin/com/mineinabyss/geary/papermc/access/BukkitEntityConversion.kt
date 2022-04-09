package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.papermc.BukkitEntityAssociationsContext
import com.mineinabyss.idofront.typealiases.BukkitEntity

context (EngineContext, BukkitEntityAssociationsContext) public fun BukkitEntity.toGeary(): GearyEntity =
    bukkit2Geary[entityId] ?: entity { set<BukkitEntity>(this@toGeary) }

// Separate function because inline `run` cannot be nullable
//TODO we want to call load entity event after init runs
//TODO inline when compiler bug fixed
context (EngineContext, BukkitEntityAssociationsContext) public fun BukkitEntity.toGeary(
    init: GearyEntity.() -> Unit
): GearyEntity = toGeary().apply { init() }

context (BukkitEntityAssociationsContext) public fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    bukkit2Geary[entityId]

context(EngineContext) public fun GearyEntity.toBukkit(): BukkitEntity? = get()
