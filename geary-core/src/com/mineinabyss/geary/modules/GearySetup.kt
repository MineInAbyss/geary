package com.mineinabyss.geary.modules

import com.mineinabyss.dependencies.DI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

/**
 * Represents a Geary engine whose dependencies have been created in a [GearyModule] and is ready to have addons
 * installed. Load phases are accessible here and will be called once start gets called.
 */
class GearySetup(
    di: DI,
) {
    val geary = Geary(di)

//    fun loggerSeverity(severity: Severity) {
//        logger.mutableConfig.minSeverity = severity
//    }

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
