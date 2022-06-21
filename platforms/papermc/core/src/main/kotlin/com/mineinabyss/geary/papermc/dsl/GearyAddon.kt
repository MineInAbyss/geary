package com.mineinabyss.geary.papermc.dsl

import com.mineinabyss.geary.addon.GearyAddon
import com.mineinabyss.geary.papermc.GearyMCContextKoin
import org.bukkit.plugin.Plugin

/** Entry point to register a new [Plugin] with the Geary ECS. */
inline fun Plugin.gearyAddon(crossinline init: GearyAddon.() -> Unit) {
    with(GearyMCContextKoin()) {
        serializers.clearSerializerModule(name)
        GearyAddon(
            namespace = this@gearyAddon.name.lowercase(),
            classLoader = this@gearyAddon::class.java.classLoader
        ).init()
    }
}
