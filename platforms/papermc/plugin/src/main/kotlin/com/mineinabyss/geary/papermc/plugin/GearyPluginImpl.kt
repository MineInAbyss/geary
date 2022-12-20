package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addon.*
import com.mineinabyss.geary.addons.GearyLoadPhase.ENABLE
import com.mineinabyss.geary.addons.dsl.AutoScan
import com.mineinabyss.geary.addons.dsl.autoscan
import com.mineinabyss.geary.addons.dsl.namespace
import com.mineinabyss.geary.addons.dsl.serializers.*
import com.mineinabyss.geary.formats.YamlFormat
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.modules.GearyArchetypeModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.modules.GearyPaperModule
import com.mineinabyss.geary.prefabs.prefabs
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


class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        val module = GearyPaperModule(
            GearyArchetypeModule(tickDuration = 1.ticks)
        )
        module.inject()
        geary {
            namespace("geary") {
                autoscan {
                    autoscan("com.mineinabyss", AutoScan::all)
                    all()
                }
                prefabs {
                    paths(dataFolder.toPath())
                }
                serialization {
                    components {
                        component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                    }
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
