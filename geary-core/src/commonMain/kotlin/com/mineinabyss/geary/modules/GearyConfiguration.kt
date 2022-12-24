package com.mineinabyss.geary.modules

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.addons.dsl.GearyDSL

@GearyDSL
class GearyConfiguration {
    inline fun <T : GearyAddonWithDefault<Module>, reified Module: Any> install(
        addon: T,
    ) = install(addon, addon.default())

    inline fun <T : GearyAddon<Module>, reified Module: Any> install(
        addon: T,
        module: Module,
    ) {
        DI.add(module)
        with(addon) { module.install() }
    }

    fun namespace(namespace: String, configure: Namespaced.() -> Unit) {
        Namespaced(namespace, TODO(), this).configure()
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
