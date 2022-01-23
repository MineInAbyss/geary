package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.withSerialName
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
import com.mineinabyss.idofront.config.singleConfig
import com.mineinabyss.idofront.config.startOrAppendKoin
import com.mineinabyss.idofront.platforms.IdofrontPlatforms
import com.mineinabyss.idofront.plugin.registerEvents
import com.mineinabyss.idofront.plugin.registerService
import com.mineinabyss.idofront.serialization.UUIDSerializer
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module
import java.util.*
import kotlin.io.path.div

public val gearyPlugin: GearyPlugin = Bukkit.getPluginManager().getPlugin("Geary") as GearyPlugin

public class GearyPlugin : JavaPlugin(), KoinComponent {
    override fun onLoad() {
        IdofrontPlatforms.load(this, "mineinabyss")
    }

    //    private var module: Module? = null
    @Suppress("RemoveExplicitTypeArguments")
    override fun onEnable() {
        instance = this
        registerEvents(StartupEventListener)

        saveDefaultConfig()
        reloadConfig()
        startOrAppendKoin(module {
            single<GearyPlugin> { this@GearyPlugin }
            single<GearyWebConsole> { GearyWebConsole() }
            single<GearyEngine> { SpigotEngine(this@GearyPlugin).apply { start() } }
            singleConfig(GearyConfig.serializer(), this@GearyPlugin)
        })

        get<GearyWebConsole>().start()

        // Register commands.
        GearyCommands()

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
        get<GearyWebConsole>().stop()
    }

    public companion object {
        /** Gets [GearyPlugin] via Bukkit once, then sends that reference back afterwards */
        public lateinit var instance: GearyPlugin
    }
}
