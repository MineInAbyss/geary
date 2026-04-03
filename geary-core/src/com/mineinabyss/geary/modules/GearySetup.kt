package com.mineinabyss.geary.modules

import co.touchlab.kermit.Severity
import com.mineinabyss.features.Feature
import com.mineinabyss.features.feature
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DirectDI
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

/**
 * Represents a Geary engine whose dependencies have been created in a [GearyModule] and is ready to have addons
 * installed. Load phases are accessible here and will be called once start gets called.
 */
class GearySetup(
    di: DirectDI,
) {
    val geary = Geary(di)
    val logger get() = geary.logger

    fun <T : Any> install(addon: Feature<T>): T {
        geary.addons.enable(addon)
        return geary.getAddon(addon)
    }

    fun <T : Any> install(addon: Feature<T>, configure: T.() -> Unit): T {
        val configuration = feature("${addon.name}-anonymous") {
            dependsOn { features(addon) }

            onEnable {
                val instance = addon.extract(this)
                configure(instance)
            }
        }
        geary.addons.enable(configuration)
        return geary.getAddon(addon)
    }

    fun loggerSeverity(severity: Severity) {
        logger.mutableConfig.minSeverity = severity
    }

    fun scheduleTicking(
        every: Duration,
        context: CoroutineContext = EmptyCoroutineContext,
    ) {
        geary.engine.mainScope.launch(context) {
            while (true) {
                geary.engine.tick()
                delay(every)
            }
        }
    }
}
