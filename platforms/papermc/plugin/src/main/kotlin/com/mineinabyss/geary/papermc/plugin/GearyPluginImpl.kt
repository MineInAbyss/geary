package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.api.addon.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.entities.UUID2GearyMap
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.withSerialName
import com.mineinabyss.geary.papermc.GearyConfig
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.StartupEventListener
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.dsl.GearyAddonManager
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.papermc.listeners.GearyAttemptSpawnListener
import com.mineinabyss.geary.papermc.store.FileSystemStore
import com.mineinabyss.geary.papermc.store.GearyStore
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.idofront.config.singleConfig
import com.mineinabyss.idofront.config.startOrAppendKoin
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.serialization.UUIDSerializer
import org.bukkit.Bukkit
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
        val engine = PaperMCEngine(this@GearyPluginImpl)
        val queryManager = QueryManager(engine)
        val bukkitEntity2Geary = BukkitEntity2Geary()
        val uuid2GearyMap = UUID2GearyMap()
        val addonManager = GearyAddonManager()
        val prefabManager = PrefabManager(engine)

        startOrAppendKoin(module {
            single<GearyPlugin> { this@GearyPluginImpl }
            single<QueryManager> { queryManager }
            single<Engine> { engine }
            single<BukkitEntity2Geary> { bukkitEntity2Geary }
            single<UUID2GearyMap> { uuid2GearyMap }
            single<GearyAddonManager> { addonManager }
            single<PrefabManager> { prefabManager }
            singleConfig(GearyConfig.serializer(), this@GearyPluginImpl)
        })

        engine.start()
        uuid2GearyMap.startTracking()
        bukkitEntity2Geary.startTracking()

        // Register commands.
        GearyCommands(this)

        registerEvents(
            GearyAttemptSpawnListener,
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
                ENABLE {
                    registerService<GearyStore>(FileSystemStore(dataFolder.toPath() / "serialized"))
                    Bukkit.getOnlinePlayers().forEach { it.toGeary() }
                }
            }
        }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}
