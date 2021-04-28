package com.mineinabyss.geary.minecraft.properties

import com.mineinabyss.geary.ecs.api.properties.EntityPropertyHolder
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.minecraft.access.toBukkit
import org.bukkit.entity.Entity

public inline fun <reified T : Entity> EntityPropertyHolder.bukkitEntity(): GearyEntity.() -> T? = {
    toBukkit<T>()
}
