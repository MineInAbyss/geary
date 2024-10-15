package com.mineinabyss.geary.addons.dsl

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.idofront.di.DIContext
import kotlin.jvm.JvmName

interface GearyAddonWithDefault<Module> : GearyAddon<Module> {
    fun default(): Module
}

interface GearyAddon<Module> {
    fun Module.install()
}

data class Addon<Configuration, Instance>(
    val name: String,
    val defaultConfiguration: Geary.() -> Configuration,
    val onInstall: Geary.(Configuration) -> Instance,
) {
    operator fun invoke(customConfiguration: Geary.() -> Configuration): Addon<Configuration, Instance> {
        return copy(defaultConfiguration = customConfiguration)
    }
}

data class AddonSetup<Configuration>(
    val name: String,
    val configuration: Configuration,
    val module: GearyModule,
    val context: DIContext,
) {
    val geary: Geary = Geary(module, context, module.logger.withTag(name))

    /** Runs a block during [GearyPhase.INIT_COMPONENTS] */
    fun components(configure: Geary.() -> Unit) {
        on(GearyPhase.INIT_COMPONENTS) {
            configure(geary)
        }
    }

    /** Runs a block during [GearyPhase.INIT_SYSTEMS] */
    fun systems(configure: Geary.() -> Unit) {
        on(GearyPhase.INIT_SYSTEMS) {
            configure(geary)
        }
    }

    /** Runs a block during [GearyPhase.INIT_ENTITIES] */
    fun entities(configure: Geary.() -> Unit) {
        on(GearyPhase.INIT_ENTITIES) {
            configure(geary)
        }
    }

    fun onStart(run: Geary.() -> Unit) {
        on(GearyPhase.ENABLE) {
            run(geary)
        }
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
        module.pipeline.runOnOrAfter(phase, run)
    }
}

fun createAddon(
    name: String,
    init: AddonSetup<Unit>.() -> Unit = {},
): Addon<Unit, Unit> = Addon(name, { }) {
    init(AddonSetup(name, it, module, context))
}

@JvmName("createAddon1")
fun <Conf> createAddon(
    name: String,
    configuration: Geary.() -> Conf,
    init: AddonSetup<Conf>.() -> Unit = {},
): Addon<Conf, Conf> = Addon(name, configuration) { conf ->
    init(AddonSetup(name, conf, module, context))
    conf
}

@JvmName("createAddon2")
fun <Conf, Inst> createAddon(
    name: String,
    configuration: Geary.() -> Conf,
    init: AddonSetup<Conf>.() -> Inst,
): Addon<Conf, Inst> {
    return Addon(name, configuration) {
        init(AddonSetup(name, it, module, context))
    }
}
