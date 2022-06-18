package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addon.*
import com.mineinabyss.geary.addon.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.datatypes.maps.UUID2GearyMap
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.formats.YamlFormat
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.papermc.GearyConfig
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.StartupEventListener
import com.mineinabyss.geary.papermc.access.BukkitEntity2Geary
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.dsl.GearyMCAddonManager
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.geary.papermc.engine.PaperMCEngine
import com.mineinabyss.geary.papermc.store.FileSystemStore
import com.mineinabyss.geary.papermc.store.GearyStore
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.serialization.GearyFormats
import com.mineinabyss.geary.serialization.GearySerializers
import com.mineinabyss.geary.systems.QueryManager
import com.mineinabyss.idofront.config.singleConfig
import com.mineinabyss.idofront.config.startOrAppendKoin
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.serialization.UUIDSerializer
import org.bukkit.Bukkit
import org.koin.core.logger.Logger
import org.koin.dsl.module
import java.util.*
import kotlin.io.path.div
import kotlin.io.path.listDirectoryEntries

class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    //    private var module: Module? = null
    @Suppress("RemoveExplicitTypeArguments")
    override fun onEnable() {
        saveDefaultConfig()
        reloadConfig()

        val engine = PaperMCEngine(this@GearyPluginImpl)
        val serializers = GearySerializers()
        val formats = GearyFormats(serializers)
        //TODO hopefully we can combine with statements in the future
        val queryManager = QueryManager()
        val uuid2GearyMap = UUID2GearyMap(engine)
        val prefabManager = PrefabManager()
        val addonManager = GearyMCAddonManager()
        val bukkitEntity2Geary = BukkitEntity2Geary()

        startOrAppendKoin(module {
            single<Logger> { GearyLogger(this@GearyPluginImpl) }
            single<GearyPlugin> { this@GearyPluginImpl }
            single<QueryManager> { queryManager }
            single<Engine> { engine }
            single<BukkitEntity2Geary> { bukkitEntity2Geary }
            single<UUID2GearyMap> { uuid2GearyMap }
            single<GearyAddonManager> { addonManager }
            single<PrefabManager> { prefabManager }
            single<GearySerializers> { serializers }
            single<GearyFormats> { formats }
            singleConfig<GearyConfig>(GearyConfig.serializer(),this@GearyPluginImpl)
        })

        engine.start()
        queryManager.init()
        uuid2GearyMap.startTracking()
        bukkitEntity2Geary.startTracking()

        gearyAddon {
            autoscan("com.mineinabyss") {
                all()
            }
            serialization {
                components {
                    component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                }
            }
            formats { module ->
                register("yml", YamlFormat(module))
            }
            prefabs {
                dataFolder.toPath().listDirectoryEntries().forEach(::path)
            }

            // Register commands
            GearyCommands()

            registerEvents(
                StartupEventListener(),
            )

            startup {
                ENABLE {
                    registerService<GearyStore>(
                        FileSystemStore(
                            dataFolder.toPath() / "serialized",
                            formats.binaryFormat
                        )
                    )
                    Bukkit.getOnlinePlayers().forEach { it.toGeary() }
                }
            }
        }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}
