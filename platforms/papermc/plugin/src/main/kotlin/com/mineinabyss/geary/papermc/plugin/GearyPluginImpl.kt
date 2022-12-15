package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addon.*
import com.mineinabyss.geary.addon.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.context.GearyArchetypeModule
import com.mineinabyss.geary.context.GearyModule
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.*
import com.mineinabyss.geary.formats.YamlFormat
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.papermc.GearyPaperModule
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.StartupEventListener
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.serialization.UUIDSerializer
import org.bukkit.Bukkit
import java.util.*
import kotlin.io.path.listDirectoryEntries

class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    //    private var module: Module? = null
    @Suppress("RemoveExplicitTypeArguments")
    override fun onEnable() {
        DI.add(GearyPaperModule(GearyArchetypeModule(), this))
        val engine = gearyPaper.engine
        engine.start()
        gearyPaper.uuid2entity.startTracking()
        gearyPaper.bukkit2Geary.startTracking()

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
