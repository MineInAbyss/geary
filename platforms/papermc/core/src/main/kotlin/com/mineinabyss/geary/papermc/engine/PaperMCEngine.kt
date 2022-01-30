package com.mineinabyss.geary.papermc.engine

import co.aikar.timings.Timings
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.idofront.plugin.registerEvents
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinComponent

public class PaperMCEngine(private val plugin: Plugin) : GearyEngine(), KoinComponent {
    public val componentsKey: NamespacedKey = NamespacedKey(plugin, "components")

    override suspend fun TickingSystem.runSystem() {
        // Adds a line in timings report showing which systems take up more time.
        val timing = Timings.ofStart(plugin, javaClass.name)
        runCatching {
            doTick()
        }.apply {
            // We want to stop the timing no matter what, but still propagate error up
            timing.stopTiming()
        }.getOrThrow()
    }

    override suspend fun addSystem(system: GearySystem) {
        super.addSystem(system)

        if (system is Listener)
            plugin.registerEvents(system)
    }

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks
        plugin.schedule {
            repeating(1)
            while (true) {
                tick(Bukkit.getServer().currentTick.toLong())
                yield()
            }
        }
    }
}
