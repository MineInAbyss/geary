package com.mineinabyss.geary.modules

import co.touchlab.kermit.Severity
import com.mineinabyss.geary.addons.dsl.GearyAddon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

/**
 * Represents a Geary engine whose dependencies have been created in a [GearyModule] and is ready to have addons
 * installed. Load phases are accessible here and will be called once start gets called.
 */
class GearySetup(
    val application: KoinApplication,
) {
    val geary = Geary(application)
    val logger get() = geary.logger

    fun <T : Any> install(addon: GearyAddon<T>): T {
        return geary.addons.install(addon)
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
