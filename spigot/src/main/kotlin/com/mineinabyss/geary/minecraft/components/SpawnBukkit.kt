package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import org.bukkit.Location

@AutoscanComponent
public data class SpawnBukkit(
    public val at: Location
)
