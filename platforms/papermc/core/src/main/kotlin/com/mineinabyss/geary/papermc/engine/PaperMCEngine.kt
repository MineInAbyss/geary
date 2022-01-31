package com.mineinabyss.geary.papermc.engine

import co.aikar.timings.Timings
import com.mineinabyss.geary.ecs.api.systems.GearySystem
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.time.ticks
import com.okkero.skedule.BukkitDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
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
            timing.stopTiming() //TODO doTick can suspend and then timings don't work
        }.getOrThrow()
    }

    override suspend fun addSystem(system: GearySystem) {
        super.addSystem(system)

        if (system is Listener)
            plugin.registerEvents(system)
    }

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks
        launch(BukkitDispatcher(plugin as JavaPlugin)) {
//        Bukkit.getScheduler().scheduleSyncRepeatingTask(, {
            while(true) {
                tick(Bukkit.getServer().currentTick.toLong())
                delay(1.ticks)
            }
        }
    }
}
