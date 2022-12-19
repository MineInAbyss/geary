package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addon.*
import com.mineinabyss.geary.addons.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.formats.YamlFormat
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.modules.GearyArchetypeModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.dsl.gearyAddon
import com.mineinabyss.geary.papermc.dsl.pluginAddon
import com.mineinabyss.geary.papermc.modules.GearyPaperModule
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.serialization.UUIDSerializer
import com.mineinabyss.idofront.time.ticks
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.bukkit.Bukkit
import java.util.*
import kotlin.io.path.listDirectoryEntries

class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        val module = GearyPaperModule(
            GearyArchetypeModule(tickDuration = 1.ticks), this
        )
        module.inject()
        geary.configure("geary") {
            install(Prefabs) {
                paths(dataFolder.toPath())
            }
            install(pluginAddon {

            })
            autoscan("com.mineinabyss", AutoScanAddon::all)
            serialization {
                components {
                    component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                }
            }
            formats { module ->
                register("yml", YamlFormat(module))
            }

            on(ENABLE) {
                Bukkit.getOnlinePlayers().forEach { it.toGeary() }
            }
        }

        // Register commands
        GearyCommands()
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(Routing) {
            options { }
        }
        routing {
            get("/") {
                call.respondText("Hello, world!")
            }
        }
    }.start(wait = true)
}
