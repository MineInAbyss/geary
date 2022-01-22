package com.mineinabyss.geary.webconsole

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

class GearyWebConsole(
    val port: Int = 9090,
    val host: String = "0.0.0.0",
) {
    private var runningServer: NettyApplicationEngine? = null

    fun start() {
        runningServer = embeddedServer(Netty, port = port, host = host) {
            routing {
                get("/hello") {
                    call.respondText("Hello world")
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        runningServer?.stop(1000, 2000)
    }
}
