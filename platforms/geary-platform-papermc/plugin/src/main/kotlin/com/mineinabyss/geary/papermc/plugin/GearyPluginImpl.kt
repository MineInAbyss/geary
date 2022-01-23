package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.withSerialName
import com.mineinabyss.geary.ecs.helpers.GearyKoinComponent
import com.mineinabyss.geary.minecraft.GearyPlugin
import com.mineinabyss.geary.minecraft.StartupEventListener
import com.mineinabyss.geary.minecraft.access.BukkitAssociations
import com.mineinabyss.geary.minecraft.access.BukkitEntityAssociations
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.geary.minecraft.dsl.GearyLoadPhase
import com.mineinabyss.geary.minecraft.dsl.gearyAddon
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.listeners.GearyAttemptSpawnListener
import com.mineinabyss.geary.minecraft.listeners.InheritPrefabsOnLoad
import com.mineinabyss.geary.minecraft.store.FileSystemStore
import com.mineinabyss.geary.minecraft.store.GearyStore
import com.mineinabyss.geary.papermc.GearyConfig
import com.mineinabyss.geary.webconsole.GearyWebConsole
import com.mineinabyss.geary.webconsole.webConsole
import com.mineinabyss.idofront.config.singleConfig
import com.mineinabyss.idofront.config.startOrAppendKoin
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.serialization.UUIDSerializer
import org.bukkit.Bukkit
import org.koin.core.component.get
import org.koin.dsl.module
import java.util.*
import kotlin.io.path.div

public class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    //    private var module: Module? = null
    @Suppress("RemoveExplicitTypeArguments")
    override fun onEnable() {
        registerEvents(StartupEventListener)

        saveDefaultConfig()
        reloadConfig()
        val webConsole = GearyWebConsole()
        val engine = SpigotEngine(this@GearyPluginImpl)
        val queryManager = QueryManager(engine)
        startOrAppendKoin(module {
            single<GearyPlugin> { this@GearyPluginImpl }
            single<GearyWebConsole> { webConsole }
            single<QueryManager> { queryManager }
            single<Engine> { engine }
            singleConfig(GearyConfig.serializer(), this@GearyPluginImpl)
        })
        engine.start()
        webConsole.start()

        // Register commands.
        GearyCommands(this)

        registerEvents(
            BukkitEntityAssociations,
            BukkitAssociations,
            GearyAttemptSpawnListener,
            InheritPrefabsOnLoad(),
        )

        // This will also register a serializer for GearyEntityType
        gearyAddon {
            autoScanAll()

            components {
                //TODO move out to a custom components class
                subclass(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                Formats.registerSerialName("geary:uuid", UUID::class)
            }

            dataFolder.listFiles()
                ?.filter { it.isDirectory }
                ?.forEach { loadPrefabs(it, namespace = it.name) }

            startup {
                GearyLoadPhase.ENABLE {
                    registerService<GearyStore>(FileSystemStore(dataFolder.toPath() / "serialized"))
                    Bukkit.getOnlinePlayers().forEach { it.toGeary() }
                }
            }
        }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        GearyKoinComponent().apply {
            webConsole.stop()
        }
    }
}
