package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.ding.DI
import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.engine.*

val geary: GearyModule by DI.observe()

@GearyDSL
interface GearyModule {
    val logger: Logger
    val entityProvider: EntityProvider
    val systems: SystemProvider
    val componentProvider: ComponentProvider

    val read: EntityReadOperations
    val write: EntityMutateOperations

    val queryManager: QueryManager
    val components: Components
    val engine: Engine

    val eventRunner: EventRunner
    val pipeline: Pipeline

    fun inject()
    fun start()

//    operator fun invoke(configure: GearyModule.() -> Unit)
}

fun geary(configure: GearyConfiguration.() -> Unit) {
}

@GearyDSL
interface GearyConfiguration {
    fun <T : GearyAddonWithDefault<Module>, Module> install(
        addon: T,
    ) = install(addon, addon.default())

    fun <T : GearyAddon<Module>, Module> install(
        addon: T,
        module: Module,
    )

    fun namespace(namespace: String, configure: Namespaced.() -> Unit) = Namespaced(namespace, TODO(), this).configure()

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
    fun on(phase: GearyPhase, run: () -> Unit)
}
