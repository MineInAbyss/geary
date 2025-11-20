package com.mineinabyss.geary.autoscan

import com.mineinabyss.geary.addons.dsl.gearyAddon
import com.mineinabyss.geary.serialization.SerializableComponents
import org.koin.core.module.dsl.scopedOf

val AutoScanAddon = gearyAddon<AutoScanner>("autoscan") {
    dependsOn(SerializableComponents)

    scopedModule {
        scopedOf(::AutoScanner)
    }

    onEnable {
//            configuration.scannedSystems.asSequence()
//                .onEach { it.call(geary) }
//                .map { it.name }
//                .let {
//                    if (geary.logger.config.minSeverity <= Severity.Verbose)
//                        geary.logger.i("Autoscan loaded singleton systems: ${it.joinToString()}")
//                    else geary.logger.i("Autoscan loaded ${it.count()} singleton systems")
//                }
    }
}