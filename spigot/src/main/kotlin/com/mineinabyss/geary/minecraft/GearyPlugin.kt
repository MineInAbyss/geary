package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.okkero.skedule.schedule
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.ExperimentalTime

public class GearyPlugin : JavaPlugin() {
    @ExperimentalCommandDSL
    @ExperimentalTime
    override fun onEnable() {
        logger.info("On enable has been called")
        saveDefaultConfig()
        reloadConfig()

        registerService<Engine>(SpigotEngine().apply { start() })

        // Register commands.
        GearyCommands()

        registerEvents(
            BukkitEntityAccess
        )

        registerSerializers()

        //Register all players with the ECS after all plugins loaded
        schedule {
            waitFor(1)
            Bukkit.getOnlinePlayers().forEach { player ->
                BukkitEntityAccess.registerEntity(player)
            }
        }

    }

    override fun onDisable() {
        super.onDisable()
        logger.info("onDisable has been invoked!")
        server.scheduler.cancelTasks(this)
    }
}
