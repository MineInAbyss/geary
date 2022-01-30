package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.papermc.GearyMCKoinComponent
import com.mineinabyss.idofront.typealiases.BukkitEntity

public suspend fun BukkitEntity.toGeary(): GearyEntity = GearyMCKoinComponent {
    bukkit2Geary[entityId] ?: entity { set<BukkitEntity>(this@toGeary) }
}

// Separate function because inline `run` cannot be nullable
//TODO we want to call load entity event after init runs
public suspend inline fun BukkitEntity.toGeary(init: GearyEntity.() -> Unit): GearyEntity =
    toGeary().apply { init() }

public fun BukkitEntity.toGearyOrNull(): GearyEntity? = GearyMCKoinComponent().run {
    bukkit2Geary[entityId]
}

public suspend fun GearyEntity.toBukkit(): BukkitEntity? = get()
