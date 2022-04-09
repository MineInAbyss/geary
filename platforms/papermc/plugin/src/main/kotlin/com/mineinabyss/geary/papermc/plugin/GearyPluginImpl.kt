package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.api.addon.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.ecs.api.FormatsContext
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.systems.QueryContext
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.entities.UUID2GearyMap
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.withSerialName
import com.mineinabyss.geary.papermc.*
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
        saveDefaultConfig()
        reloadConfig()

        val engine = PaperMCEngine(this@GearyPluginImpl)
        val engineContext = object: PaperEngineContext {
            override val engine = engine
        }
        val pluginContext = object : PluginContext {
            override val geary = this@GearyPluginImpl
        }
        val formats = Formats()
        val formatsContext = object : FormatsContext {
            override val formats = formats
        }
        //TODO hopefully we can combine with statements in the future
        with(engineContext) {
            with(pluginContext) {
                with(formatsContext) {
                    val queryManager = QueryManager()
                    val uuid2GearyMap = UUID2GearyMap()
                    val prefabManager = PrefabManager(engine)
                    val queryContext = object: QueryContext {
                        override val queryManager = queryManager
                    }
                    with(queryContext) {
                        val addonManager = GearyAddonManager()
                        val bukkitEntity2Geary = BukkitEntity2Geary()

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
                        queryManager.init()
                        uuid2GearyMap.startTracking()
                        bukkitEntity2Geary.startTracking()
                    }
                }
            }
        }

        gearyAddon {
            autoScanAll()

            components {
                //TODO move out to a custom components class
                subclass(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                formats.registerSerialName("geary:uuid", UUID::class)
            }

            dataFolder.listFiles()
                ?.filter { it.isDirectory }
                ?.forEach { loadPrefabs(it, namespace = it.name) }

            // Register commands.
            GearyCommands()

            registerEvents(
                StartupEventListener,
                GearyAttemptSpawnListener,
            )

            startup {
                ENABLE {
                    registerService<GearyStore>(FileSystemStore(dataFolder.toPath() / "serialized", formats.cborFormat))
                    Bukkit.getOnlinePlayers().forEach { it.toGeary() }
                }
            }
        }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}
