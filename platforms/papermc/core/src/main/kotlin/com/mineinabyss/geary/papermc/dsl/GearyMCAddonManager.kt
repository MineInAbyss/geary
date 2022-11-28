package com.mineinabyss.geary.papermc.dsl

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.addon.GearyAddonManager
import com.mineinabyss.geary.papermc.GearyMCContext
import com.mineinabyss.geary.papermc.GearyPaperModule
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GearyMCAddonManager : GearyAddonManager(), GearyMCContext by GearyPaperModule() {
    override fun scheduleLoadTasks() {
        // On sync thread because after one server tick, all other plugins are guaranteed to have loaded
        engine.launch(geary.minecraftDispatcher) {
            delay(1.ticks)
            load()
            delay(1.ticks)
            enableAddons()
        }
    }
}
