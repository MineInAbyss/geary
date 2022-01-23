package com.mineinabyss.geary.webconsole

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.helpers.listComponents
import com.mineinabyss.geary.minecraft.access.toGeary
import com.mineinabyss.geary.webconsole.data.EntityInfo
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import org.bukkit.Bukkit
import org.slf4j.LoggerFactory

class GearyWebConsole(
    val port: Int = 9090,
    val host: String = "0.0.0.0",
) {
    private var runningServer: NettyApplicationEngine? = null

    fun start() {
        runningServer = embeddedServer(Netty, environment = applicationEngineEnvironment {
            // Ktor is loaded in our library which is a different classloader
            classLoader = GearyWebConsole::class.java.classLoader
            this.parentCoroutineContext = coroutineContext + parentCoroutineContext
            this.log = LoggerFactory.getLogger("ktor.application")

            connector {
                port = this@GearyWebConsole.port
                host = this@GearyWebConsole.host
            }

            module {
                install(ContentNegotiation) {
                    json()
                }
                install(CORS) {
                    method(HttpMethod.Get)
                    method(HttpMethod.Post)
                    method(HttpMethod.Delete)
                    anyHost()
                }

                install(Compression) {
                    gzip()
                }

                routing {
                    get("/") {
                        call.respondText(
                            GearyWebConsole::class.java.classLoader.getResource("index.html")!!.readText(),
                            ContentType.Text.Html
                        )
                    }
                    static("/") { resources("") }
                    get("/hello") {
                        call.respondText("Hello world")
                    }
                    route(EntityInfo.path) {
                        fun infoFor(entity: GearyEntity) = EntityInfo(entity.listComponents())
                        get("/id/{id}") {
                            val entity = call.parameters["id"]?.toULong()?.toGeary() ?: error("Invalid get request")
                            call.respond(infoFor(entity))
                        }
                        get("/player/{name}") {
                            val player =
                                Bukkit.getPlayer(call.parameters["name"]!!) ?: error("No player with that name")
                            val entity = player.toGeary()
                            call.respond(infoFor(entity))
                        }
                    }
                }
            }
        }).start(wait = false)
    }

    fun stop() {
        runningServer?.stop(1000, 2000)
    }
}
