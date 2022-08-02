package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.globalContextMC
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Player

fun BukkitEntity.toGeary(): GearyEntity {
    if(this is Player && !isOnline) error("Tried to access Geary entity for offline player: $name")
    return globalContextMC.bukkit2Geary[entityId] ?: entity { set<BukkitEntity>(this@toGeary) }
}

//TODO perhaps add a function literal that fires before any entity create events do
inline fun BukkitEntity.toGeary(
    init: GearyEntity.() -> Unit
): GearyEntity = toGeary().apply { init() }

fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    globalContextMC.bukkit2Geary[entityId]

fun GearyEntity.toBukkit(): BukkitEntity? = get()
