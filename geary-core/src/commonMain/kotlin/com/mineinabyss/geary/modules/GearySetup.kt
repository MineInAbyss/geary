package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.Addon
import org.koin.core.KoinApplication

/**
 * Represents a Geary engine whose dependencies have been created in a [GearyModule] and is ready to have addons
 * installed. Load phases are accessible here and will be called once start gets called.
 */
class GearySetup(
    val application: KoinApplication,
) {
    val logger = application.koin.get<Logger>()
    val geary = Geary(application)

    inline fun <T : Addon<Conf, *>, Conf> install(addon: T, configure: Conf.() -> Unit = {}): T {
        geary.addons.getOrPut(geary, addon).apply { config.configure() }
        return addon
    }

    fun namespace(namespace: String, configure: Namespaced.() -> Unit) {
        Namespaced(namespace, this).configure()
    }
}
