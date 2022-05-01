package com.mineinabyss.geary.papermc

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

public class StartupEventListener : Listener {
    private val runPostLoad: MutableList<() -> Unit> = mutableListOf()

    @EventHandler
    public fun PluginEnableEvent.onPluginLoad() {
        if ("Geary" in plugin.description.depend && getGearyDependants().last() == plugin) {
            runPostLoad.toList().forEach { it() }
        }
    }

    public companion object {
        public fun getGearyDependants(): List<Plugin> =
            Bukkit.getServer().pluginManager.plugins.filter { "Geary" in it.description.depend }
    }
}
