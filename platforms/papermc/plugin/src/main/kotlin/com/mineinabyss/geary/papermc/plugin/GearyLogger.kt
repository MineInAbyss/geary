package com.mineinabyss.geary.papermc.plugin

import org.bukkit.plugin.Plugin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

public class GearyLogger(private val plugin: Plugin) : Logger() {
    override fun log(level: Level, msg: MESSAGE) {
        plugin.logger.log(
            when (level) {
                Level.DEBUG -> java.util.logging.Level.FINE
                Level.INFO -> java.util.logging.Level.INFO
                Level.ERROR -> java.util.logging.Level.SEVERE
                Level.NONE -> java.util.logging.Level.OFF
            }, msg
        )
    }
}
