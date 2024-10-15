package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.Addon
import com.mineinabyss.idofront.di.DIContext

/**
 * Represents a Geary engine whose dependencies have been created in a [GearyModule] and is ready to have addons
 * installed. Load phases are accessible here and will be called once start gets called.
 */
class GearySetup(
    val module: GearyModule,
    val context: DIContext,
) {
    val addons = MutableAddons()
    val logger = module.logger
    val geary = Geary(module, context, logger)

    inline fun <T: Addon<Conf, *>, Conf> install(addon: T, configure: Conf.() -> Unit = {}): T {
        addons.getOrPut(geary, addon).apply { config.configure() }
        return addon
    }

    fun namespace(namespace: String, configure: Namespaced.() -> Unit) {
        Namespaced(namespace, this).configure()
    }
}
