package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.systems.PassiveActionsSystem
import com.mineinabyss.geary.minecraft.access.BukkitEntityAccess
import com.mineinabyss.geary.minecraft.components.PlayerComponent
import com.mineinabyss.geary.minecraft.dsl.attachToGeary
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.okkero.skedule.schedule
import kotlinx.serialization.InternalSerializationApi
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.ExperimentalTime

public class GearyPlugin : JavaPlugin() {
    @InternalSerializationApi
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

        // This will also register a serializer for GearyEntityType
        attachToGeary {
            autoscanComponents()
            autoscanConditions()
            autoscanActions()

            systems(
                PassiveActionsSystem
            )

            bukkitEntityAccess {
                onEntityRegister<Player> { player ->
                    add(PlayerComponent(player.uniqueId))
                }
            }
        }

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
