package com.mineinabyss.geary.modules

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.idofront.di.DI
import kotlin.reflect.KClass

@GearyDSL
class GearyConfiguration {
    val installedAddons = mutableMapOf<KClass<out GearyAddon<*>>, Any>()
    inline fun <T : GearyAddonWithDefault<Module>, reified Module : Any> install(
        addon: T,
    ): Module {
        val module = DI.getOrNull<Module>()
        if(module != null) return module
        return install(addon, addon.default())
    }

    inline fun <T : GearyAddon<Module>, reified Module : Any> install(
        addon: T,
        module: Module,
    ): Module = with(addon) {
        DI.add(module)
        module.install()
    }.let { module }

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
