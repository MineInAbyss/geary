package com.mineinabyss.geary.minecraft.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BarColor

/**
 * > geary:bossbar
 *
 * Holds properties of a boss bar (colour, style, range)
 */
@Serializable
@SerialName("geary:bossbar")
@AutoscanComponent
public class DisplayBossBar(
    public val color: BarColor,
    public val style: BarStyle,
    public val range: Double
) {
}
