package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.addons.dsl.GearyDSL

@GearyDSL
class GearyConfiguration {
    fun <T : GearyAddonWithDefault<Module>, Module : Any> install(
        addon: T,
    ): Module = install(addon, addon.default())

    //TODO this should get or install if not present
    fun <T : GearyAddon<Module>, Module : Any> install(
        addon: T,
        module: Module,
    ): Module = with(addon) { module.install() }.let { module }

    fun namespace(namespace: String, configure: Namespaced.() -> Unit) {
        Namespaced(namespace, this).configure()
    }

    /**
     * Allows defining actions that should run at a specific phase during startup
     *
     * Within its context, invoke a [GearyPhase] to run something during it, ex:
     *
     * ```
     * GearyLoadPhase.ENABLE {
     *     // run code here
     * }
     * ```
     */

    fun on(phase: GearyPhase, run: () -> Unit) {
        geary.pipeline.intercept(phase, run)
    }
}
