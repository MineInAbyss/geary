package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.entity.EntityType

@Serializable
@SerialName("minecraft:entity_type")
@AutoscanComponent
public class BukkitEntityType(
    public val type: EntityType
)
