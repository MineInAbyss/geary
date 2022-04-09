package com.mineinabyss.geary.papermc.access

import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import org.bukkit.entity.Entity

//Split into separate files with BukkitEntityComponent for Java interoperability reasons.

context(EngineContext) public inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get<Entity>() as? T
