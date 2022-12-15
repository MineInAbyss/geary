package com.mineinabyss.geary.papermc

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin

class StartupEventListener : Listener {
    private val runPostLoad: MutableList<() -> Unit> = mutableListOf()

    @EventHandler
    fun PluginEnableEvent.onPluginLoad() {
        if ("Geary" in plugin.description.depend && getGearyDependants().last() == plugin) {
            runPostLoad.toList().forEach { it() }
        }
    }

    companion object {
        fun getGearyDependants(): List<Plugin> =
            Bukkit.getServer().pluginManager.plugins.filter { "Geary" in it.description.depend }
    }
}
