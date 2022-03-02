package com.mineinabyss.geary.papermc.dsl

import com.mineinabyss.geary.api.addon.AbstractAddonManager
import com.mineinabyss.geary.papermc.GearyPlugin
import com.okkero.skedule.schedule
import org.koin.core.component.inject

public class GearyAddonManager() : AbstractAddonManager() {
    private val plugin by inject<GearyPlugin>()

    override fun scheduleLoadTasks() {
//        plugin.schedule {
//            // After one server tick, all plugins are guaranteed to have loaded
//            waitFor(1)
//            load()
//            waitFor(1)
//            enableAddons()
//        }
    }
}
