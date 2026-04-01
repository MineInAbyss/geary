package com.mineinabyss.geary.autoscan

import com.mineinabyss.features.feature
import com.mineinabyss.geary.serialization.SerializableComponents
import org.kodein.di.bindSingletonOf

val AutoScanAddon = feature<AutoScanner>("autoscan") {
    dependsOn {
        features(SerializableComponents)
    }

    dependencies {
        bindSingletonOf(::AutoScanner)
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