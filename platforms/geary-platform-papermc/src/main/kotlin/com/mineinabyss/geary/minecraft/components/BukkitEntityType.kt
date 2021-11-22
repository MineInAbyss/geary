package com.mineinabyss.geary.minecraft.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.EntityType

@Serializable
@SerialName("minecraft:entity_type")
public class BukkitEntityType(
    public val type: EntityType
)
