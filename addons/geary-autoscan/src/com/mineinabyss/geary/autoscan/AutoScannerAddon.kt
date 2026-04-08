package com.mineinabyss.geary.autoscan

import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.addons.gearyAddon
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.SerializableComponents

val AutoScanAddon = gearyAddon("autoscan") {
    import(singleModule(SerializableComponents))
    single { new(::AutoScanner) }
//            configuration.scannedSystems.asSequence()
//                .onEach { it.call(geary) }
//                .map { it.name }
//                .let {
//                    if (geary.logger.config.minSeverity <= Severity.Verbose)
//                        geary.logger.i("Autoscan loaded singleton systems: ${it.joinToString()}")
//                    else geary.logger.i("Autoscan loaded ${it.count()} singleton systems")
//                }
}.gets<AutoScanner>()


fun Geary.autoscan(configure: AutoScanner.() -> Unit) =
    scope.load(AutoScanAddon, configure)
