package com.mineinabyss.geary.papermc.engine

import com.mineinabyss.geary.engine.SystemProvider
import com.mineinabyss.geary.systems.System
import com.mineinabyss.idofront.plugin.listeners
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class PaperSystemProvider(
    val plugin: Plugin,
    val provider: SystemProvider
) : SystemProvider by provider {
    override fun add(system: System) {
        provider.add(system)
        if (system is Listener) plugin.listeners(system)
    }
}
