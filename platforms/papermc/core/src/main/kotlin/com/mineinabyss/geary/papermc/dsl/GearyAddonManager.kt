package com.mineinabyss.geary.papermc.dsl

import com.mineinabyss.geary.api.addon.AbstractAddonManager
import com.mineinabyss.geary.papermc.GearyMCContext
import com.mineinabyss.geary.papermc.GearyMCContextKoin
import com.mineinabyss.idofront.time.ticks
import com.okkero.skedule.BukkitDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public class GearyAddonManager : AbstractAddonManager(), GearyMCContext by GearyMCContextKoin() {
    override fun scheduleLoadTasks() {
        // On sync thread because after one server tick, all other plugins are guaranteed to have loaded
        engine.launch(BukkitDispatcher(geary)) {
            delay(1.ticks)
            load()
            delay(1.ticks)
            enableAddons()
        }
    }
}
