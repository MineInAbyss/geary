package com.mineinabyss.geary.papermc.dsl

import com.mineinabyss.geary.api.addon.AbstractAddonManager
import com.mineinabyss.geary.papermc.GearyScope
import com.mineinabyss.geary.papermc.GearyScopeMC
import com.mineinabyss.idofront.time.ticks
import com.okkero.skedule.BukkitDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

public class GearyAddonManager() : AbstractAddonManager(), GearyScope by GearyScopeMC() {
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
