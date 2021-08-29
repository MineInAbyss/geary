package com.mineinabyss.geary.minecraft

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.services.GearyServiceProvider
import com.mineinabyss.geary.ecs.api.services.GearyServices
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.withSerialName
import com.mineinabyss.geary.minecraft.access.BukkitAssociations
import com.mineinabyss.geary.minecraft.access.BukkitEntityAssociations
import com.mineinabyss.geary.minecraft.access.geary
import com.mineinabyss.geary.minecraft.dsl.gearyAddon
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.listeners.GearyAttemptSpawnListener
import com.mineinabyss.geary.minecraft.store.FileSystemStore
import com.mineinabyss.geary.minecraft.store.GearyStore
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.serialization.UUIDSerializer
import com.mineinabyss.idofront.slimjar.LibraryLoaderInjector
import kotlinx.serialization.InternalSerializationApi
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.io.path.div
import kotlin.reflect.KClass

public object StartupEventListener : Listener {
    public val runPostLoad: MutableList<() -> Unit> = mutableListOf()

    public fun getGearyDependants(): List<Plugin> =
        Bukkit.getServer().pluginManager.plugins.filter { "Geary" in it.description.depend }

    @EventHandler
    public fun PluginEnableEvent.onPluginLoad() {
        if ("Geary" in plugin.description.depend && getGearyDependants().last() == plugin) {
            runPostLoad.toList().forEach { it() }
        }
    }
}

public class GearyPlugin : JavaPlugin() {
    @InternalSerializationApi
    @ExperimentalCommandDSL
    override fun onEnable() {
        LibraryLoaderInjector.inject(this)
        registerEvents(StartupEventListener)

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
            BukkitEntityAssociations,
            BukkitAssociations,
            GearyAttemptSpawnListener,
        )

        // This will also register a serializer for GearyEntityType
        gearyAddon {
            autoscanComponents()
            autoscanConditions()
            autoscanActions()

            components {
                //TODO move out to a custom components class
                subclass(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                Formats.registerSerialName("geary:uuid", UUID::class)
            }

            postLoad {
                logger.info("Loading prefabs")
                dataFolder.listFiles()
                    ?.filter { it.isDirectory }
                    ?.forEach { loadPrefabs(it, namespace = it.name) }
                Bukkit.getOnlinePlayers().forEach { geary(it) }
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        logger.info("onDisable has been invoked!")
        server.scheduler.cancelTasks(this)
    }
}
