package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.globalContextMC
import com.mineinabyss.idofront.typealiases.BukkitEntity

fun BukkitEntity.toGeary(): GearyEntity =
    globalContextMC.bukkit2Geary[entityId] ?: entity { set<BukkitEntity>(this@toGeary) }

//TODO perhaps add a function literal that fires before any entity create events do
inline fun BukkitEntity.toGeary(
    init: GearyEntity.() -> Unit
): GearyEntity = toGeary().apply { init() }

fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    globalContextMC.bukkit2Geary[entityId]

fun GearyEntity.toBukkit(): BukkitEntity? = get()
