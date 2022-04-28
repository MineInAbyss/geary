package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.globalContextMC
import com.mineinabyss.idofront.typealiases.BukkitEntity

public fun BukkitEntity.toGeary(): GearyEntity =
    globalContextMC.bukkit2Geary[entityId] ?: entity { set<BukkitEntity>(this@toGeary) }

// Separate function because inline `run` cannot be nullable
//TODO we want to call load entity event after init runs
//TODO inline when compiler bug fixed
public fun BukkitEntity.toGeary(
    init: GearyEntity.() -> Unit
): GearyEntity = toGeary().apply { init() }

public fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    globalContextMC.bukkit2Geary[entityId]

public fun GearyEntity.toBukkit(): BukkitEntity? = get()
