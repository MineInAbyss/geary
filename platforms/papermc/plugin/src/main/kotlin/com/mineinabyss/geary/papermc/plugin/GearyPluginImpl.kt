package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addon.*
import com.mineinabyss.geary.addon.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.datatypes.maps.HashTypeMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.datatypes.maps.UUID2GearyMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.engine.EventRunner
import com.mineinabyss.geary.engine.archetypes.ArchetypeEventRunner
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.EntityByArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.SimpleArchetypeProvider
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
import com.mineinabyss.geary.papermc.globalContextMC
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.serialization.Formats
import com.mineinabyss.geary.serialization.Serializers
import com.mineinabyss.geary.systems.QueryManager
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.config.singleConfig
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.plugin.startOrAppendKoin
import com.mineinabyss.idofront.serialization.UUIDSerializer
import org.bukkit.Bukkit
import org.koin.core.logger.Logger
import org.koin.dsl.module
import java.util.*
import kotlin.io.path.listDirectoryEntries

class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    //    private var module: Module? = null
    @Suppress("RemoveExplicitTypeArguments")
    override fun onEnable() {
        startOrAppendKoin(module {
            single<Logger> { GearyLogger(this@GearyPluginImpl) }
            single<GearyPlugin> { this@GearyPluginImpl }
            single<QueryManager> { QueryManager() }
            single<TypeMap> { HashTypeMap() }
            single<EventRunner> { ArchetypeEventRunner() }
            single<EntityProvider> { EntityByArchetypeProvider() }
            single<ArchetypeProvider> { SimpleArchetypeProvider() }
            single<Engine> { PaperMCEngine(this@GearyPluginImpl) }
            single<BukkitEntity2Geary> { BukkitEntity2Geary() }
            single<UUID2GearyMap> { UUID2GearyMap() }
            single<GearyAddonManager> { GearyMCAddonManager() }
            single<PrefabManager> { PrefabManager() }
            single<Serializers> { Serializers() }
            single<Formats> { Formats() }
            single<Components> { Components() }
            singleConfig(config<GearyConfig>("config") {
                fromPluginPath(loadDefault = true)
            })
        })
        val engine = globalContextMC.engine
        engine.start()
        globalContextMC.queryManager.init(engine)
        globalContextMC.uuid2entity.startTracking()
        globalContextMC.bukkit2Geary.startTracking()

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

            listeners(
                StartupEventListener(),
            )

            startup {
                ENABLE {
                    Bukkit.getOnlinePlayers().forEach { it.toGeary() }
                }
            }
        }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}
