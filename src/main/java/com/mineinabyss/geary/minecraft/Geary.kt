package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.engine.Engine
import com.mineinabyss.geary.ecs.engine.EngineImpl
import com.mineinabyss.geary.minecraft.store.BukkitEntityAccess
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerService
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.ExperimentalTime

/** Gets [Geary] via Bukkit once, then sends that reference back afterwards */
public val geary: Geary by lazy { JavaPlugin.getPlugin(Geary::class.java) }

public class Geary : JavaPlugin() {
    @ExperimentalCommandDSL
    @ExperimentalTime
    override fun onEnable() {
        logger.info("On enable has been called")
//        saveDefaultConfig()
//        reloadConfig()

        registerService<Engine>(EngineImpl())

        GearyCommands

        //Register all players with the ECS
        Bukkit.getOnlinePlayers().forEach { player ->
            BukkitEntityAccess.registerPlayer(player)
        }
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("onDisable has been invoked!")
        server.scheduler.cancelTasks(this)
    }
}
