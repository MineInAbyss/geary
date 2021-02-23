package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.autoscan.AutoscanComponent
import org.bukkit.Location

@AutoscanComponent
public data class SpawnBukkit(
    public val at: Location
)
