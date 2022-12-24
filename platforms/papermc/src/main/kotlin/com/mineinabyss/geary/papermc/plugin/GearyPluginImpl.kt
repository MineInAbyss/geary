package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.addons.GearyPhase.ENABLE
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.modules.GearyArchetypeModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.access.toGeary
import com.mineinabyss.geary.papermc.modules.GearyPaperModule
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.serialization.FileSystemAddon
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.idofront.platforms.Platforms
import com.mineinabyss.idofront.serialization.UUIDSerializer
import com.mineinabyss.idofront.time.ticks
import com.mineinabyss.serialization.formats.YamlFormat
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.bukkit.Bukkit
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name


class GearyPluginImpl : GearyPlugin() {
    override fun onLoad() {
        Platforms.load(this, "mineinabyss")
    }

    override fun onEnable() {
        val module = GearyPaperModule(
            GearyArchetypeModule(tickDuration = 1.ticks),
            this
        )
        module.inject()

        geary {
            install(FileSystemAddon, FileSystem.SYSTEM)
            install(YamlFormat)
            install(JsonFormat)
            namespace("geary") {
                serialization {
                    format("yml", ::YamlFormat)

                    components {
                        component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
                    }
                }
                autoscan("com.mineinabyss") {
                    components()
                }
            }
            // Load prefabs in Geary folder, each subfolder is considered its own namespace
            dataFolder.toPath().listDirectoryEntries()
                .filter { it.isDirectory() }
                .forEach { folder ->
                    namespace(folder.name) {
                        prefabs {
                            fromRecursive(folder.toOkioPath())
                        }
                    }
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
