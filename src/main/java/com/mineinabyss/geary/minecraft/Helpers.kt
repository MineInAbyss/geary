package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.minecraft.config.GearyConfig
import com.mineinabyss.idofront.messaging.broadcast
import org.bukkit.plugin.java.JavaPlugin


/** Gets [Geary] via Bukkit once, then sends that reference back afterwards */
public val geary: Geary by lazy { JavaPlugin.getPlugin(Geary::class.java) }

internal fun debug(message: Any?) {
    if (GearyConfig.data.debug) broadcast(message)
}
