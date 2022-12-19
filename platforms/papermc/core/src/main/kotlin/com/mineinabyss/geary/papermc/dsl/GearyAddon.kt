package com.mineinabyss.geary.papermc.dsl

import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyDSL
import org.bukkit.plugin.Plugin

/** Entry point to register a new [Plugin] with the Geary ECS. */
inline fun Plugin.pluginAddon(crossinline init: GearyDSL.() -> Unit): GearyAddon {
    return GearyAddon(
        namespace = this.name.lowercase(),
        classLoader = this::class.java.classLoader
    ).apply(init)
}
