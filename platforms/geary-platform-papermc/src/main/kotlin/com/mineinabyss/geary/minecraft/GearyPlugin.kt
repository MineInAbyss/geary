package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.services.GearyServiceProvider
import com.mineinabyss.geary.ecs.api.services.GearyServices
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.withSerialName
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
import com.mineinabyss.idofront.serialization.UUIDSerializer
import io.github.slimjar.app.builder.ApplicationBuilder
import io.github.slimjar.injector.loader.InjectableFactory
import io.github.slimjar.injector.loader.UnsafeInjectable
import kotlinx.serialization.InternalSerializationApi
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URLClassLoader
import java.util.*
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime

public class GearyPlugin : JavaPlugin() {
    @InternalSerializationApi
    @ExperimentalCommandDSL
    @ExperimentalTime
    override fun onEnable() {
        logger.info("Downloading dependencies.")
        val injector = UnsafeInjectable.create(classLoader as URLClassLoader)
        ApplicationBuilder.injecting("Geary", injector)
            .downloadDirectoryPath(File(dataFolder.parentFile, "libraries").toPath())
            .build()

        File(dataFolder, "addons").walk()
            .filter { it.extension == "jar" }
            .forEach { file ->
                runCatching {
                    val url = file.toURI().toURL()
                    injector.inject(url)
                }
                    .onSuccess { logger.info("Successfully loaded addon ${file.name}") }
                    .onFailure {
                        logger.info("Failed to load addon ${file.name}")
                        it.printStackTrace()
                    }
            }

        saveDefaultConfig()
        reloadConfig()
        GearyServices.setServiceProvider(object : GearyServiceProvider {
            override fun <T : Any> getService(service: KClass<T>): T? {
                return Bukkit.getServer().servicesManager.load(service.java)
            }
        })

        registerService<Engine>(SpigotEngine().apply { start() })

        // Register commands.
        GearyCommands()

        registerEvents(
            BukkitEntityAssociations,
            BukkitAssociations,
        )

        // This will also register a serializer for GearyEntityType
        attachToGeary {
            autoscanComponents()
            autoscanConditions()
            autoscanActions()

            components {
                //TODO move out to a custom components class
                subclass(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                Formats.registerSerialName("geary:uuid", UUID::class)
            }

            startup {
                GearyLoadPhase.ENABLE {
                    registerService<GearyStore>(FileSystemStore(dataFolder.toPath() / "serialized"))
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
