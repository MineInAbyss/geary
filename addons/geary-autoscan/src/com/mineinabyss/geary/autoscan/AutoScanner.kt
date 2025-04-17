package com.mineinabyss.geary.autoscan

import co.touchlab.kermit.Severity
import com.mineinabyss.geary.addons.dsl.createAddon
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

val AutoScanAddon = createAddon("Auto Scan", { AutoScanner() }) {
    systems {
        configuration.scannedSystems.asSequence()
            .onEach { it.call(geary) }
            .map { it.name }
            .let {
                if (geary.logger.config.minSeverity <= Severity.Verbose)
                    geary.logger.i("Autoscan loaded singleton systems: ${it.joinToString()}")
                else geary.logger.i("Autoscan loaded ${it.count()} singleton systems")
            }
    }
}

class AutoScanner(
    val scannedComponents: MutableSet<KClass<*>> = mutableSetOf(),
    val scannedSystems: MutableSet<KFunction<*>> = mutableSetOf(),
)
