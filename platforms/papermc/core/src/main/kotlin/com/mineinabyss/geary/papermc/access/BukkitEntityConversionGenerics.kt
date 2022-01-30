package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.Entity

//Split into separate files with BukkitEntityComponent for Java interoperability reasons.

public suspend inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get<Entity>() as? T
