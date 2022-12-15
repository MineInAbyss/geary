package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addon.*
import com.mineinabyss.geary.addon.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.context.GearyArchetypeModule
import com.mineinabyss.geary.formats.YamlFormat
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.papermc.GearyPaperModule
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.StartupEventListener
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.plugin.listeners
import com.mineinabyss.idofront.serialization.UUIDSerializer
import com.mineinabyss.idofront.time.ticks
import org.bukkit.Bukkit
import java.util.*
import kotlin.io.path.listDirectoryEntries

class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        val module = GearyPaperModule(GearyArchetypeModule(tickDuration = 1.ticks), this)
        module.inject()
        module.start()

        gearyAddon {
            autoscan("com.mineinabyss", AutoScanAddon::all)
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
