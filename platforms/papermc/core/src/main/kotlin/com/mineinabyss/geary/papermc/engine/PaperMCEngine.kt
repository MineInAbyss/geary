package com.mineinabyss.geary.papermc.engine

import co.aikar.timings.Timings
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.engine.archetypes.ArchetypeEngine
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey

class PaperMCEngine : ArchetypeEngine(tickDuration = 1.ticks) {
    private val plugin get() = gearyPaper.plugin

    val componentsKey: NamespacedKey = NamespacedKey(plugin, "components")

    override suspend fun RepeatingSystem.runSystem() {
        // Adds a line in timings report showing which systems take up more time.
        val timing = Timings.ofStart(plugin, javaClass.name)
        runCatching {
            doTick()
        }.apply {
            // We want to stop the timing no matter what, but still propagate error up
            timing.stopTiming()
        }.getOrThrow()
    }

    override fun scheduleSystemTicking() {
        //tick all systems every interval ticks
        launch(plugin.minecraftDispatcher) {
            while (true) {
                tick(Bukkit.getServer().currentTick.toLong())
                yield()
            }
        }
    }
}
