package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.papermc.GearyMCKoinComponent
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Entity

public fun BukkitEntity.toGeary(): GearyEntity = GearyMCKoinComponent {
    bukkitAssociations[entityId] ?: entity { set<BukkitEntity>(this@toGeary) }
}

// Separate function because inline `run` cannot be nullable
//TODO we want to call load entity event after init runs
public inline fun BukkitEntity.toGeary(init: GearyEntity.() -> Unit): GearyEntity =
    toGeary().apply { init() }

public fun BukkitEntity.toGearyOrNull(): GearyEntity? = GearyMCKoinComponent().run {
    bukkitAssociations[entityId]
}

public fun GearyEntity.toBukkit(): BukkitEntity? = get()
