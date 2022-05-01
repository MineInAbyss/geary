package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.entity.Entity

//Split into separate files with BukkitEntityComponent for Java interoperability reasons.

public inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get<Entity>() as? T
