package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.services.GearyServiceProvider
import com.mineinabyss.geary.ecs.api.services.GearyServices
import com.mineinabyss.geary.ecs.systems.ExpiringComponentSystem
import com.mineinabyss.geary.ecs.systems.PassiveActionsSystem
import com.mineinabyss.geary.minecraft.access.BukkitAssociations
import com.mineinabyss.geary.minecraft.access.BukkitEntityAssociations
import com.mineinabyss.geary.minecraft.dsl.GearyLoadManager
import com.mineinabyss.geary.minecraft.dsl.GearyLoadPhase
import com.mineinabyss.geary.minecraft.dsl.attachToGeary
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.store.FileSystemStore
import com.mineinabyss.geary.minecraft.store.GearyStore
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import kotlinx.serialization.InternalSerializationApi
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime

public class GearyPlugin : JavaPlugin() {
    @InternalSerializationApi
    @ExperimentalCommandDSL
    @ExperimentalTime
    override fun onEnable() {
        logger.info("On enable has been called")
        saveDefaultConfig()
        reloadConfig()
        GearyServices.setServiceProvider(object : GearyServiceProvider {
            override fun <T : Any> getService(service: KClass<T>): T? {
                return Bukkit.getServer().servicesManager.load(service.java)
            }
        })

        registerService<Engine>(SpigotEngine().apply { start() })
        registerService<GearyStore>(FileSystemStore(dataFolder.toPath() / "serialized"))

        // Register commands.
        GearyCommands()

        registerEvents(
            BukkitEntityAssociations
        )

        // This will also register a serializer for GearyEntityType
        attachToGeary {
            autoscanComponents()
            autoscanConditions()
            autoscanActions()

            systems(
                PassiveActionsSystem,
                ExpiringComponentSystem,
            )

            startup {
                GearyLoadPhase.ENABLE {
                    //TODO register players
//                    Bukkit.getOnlinePlayers().forEach { player ->
//                        BukkitAssociations.register(player)
//                    }
                }
            }
        }

        GearyLoadManager.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("onDisable has been invoked!")
        server.scheduler.cancelTasks(this)
    }
}
