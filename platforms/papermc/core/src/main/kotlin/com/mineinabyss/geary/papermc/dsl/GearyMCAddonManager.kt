package com.mineinabyss.geary.papermc.dsl

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.addons.AddonManager
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GearyMCAddonManager : AddonManager() {
    override fun scheduleLoadTasks() {
        // On sync thread because after one server tick, all other plugins are guaranteed to have loaded
        geary.engine.launch(gearyPaper.plugin.minecraftDispatcher) {
            delay(1.ticks)
            load()
            delay(1.ticks)
            enableAddons()
        }
    }
}
