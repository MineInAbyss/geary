package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.dsl.*
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.engine.archetypes.EntityRemove
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.di.DIContext

//val geary: GearyAPI by DI.observe()

fun <T : GearyModule> geary(
    moduleProvider: GearyModuleProviderWithDefault<T>,
    context: DIContext = DI,
    configure: GearySetup.() -> Unit = {},
): UninitializedGearyModule {
    val module = moduleProvider.default()
    return geary(moduleProvider, module, context, configure)
}

fun <T : GearyModule> geary(
    moduleProvider: GearyModuleProvider<T>,
    module: T,
    context: DIContext = DI,
    configure: GearySetup.() -> Unit = {},
): UninitializedGearyModule {
    moduleProvider.init(module)
    val setup = GearySetup(module, context)
    configure(setup)
    return UninitializedGearyModule(setup, moduleProvider as GearyModuleProvider<GearyModule>)
}

data class UninitializedGearyModule(
    val setup: GearySetup,
    val provider: GearyModuleProvider<GearyModule>
) {
    inline fun configure(configure: GearySetup.() -> Unit) = setup.configure()

    fun start(): Geary{
        setup.addons.initAll(setup)
        provider.start(setup.module)
        setup.module.pipeline.runStartupTasks() // TODO keep pipeline separate, it shouldnt be used after init
        return Geary(setup.module, setup.context)
    }
}

/**
 * Describes all the dependencies needed to initialize and run a Geary engine.
 */
@GearyDSL
interface GearyModule {
    val logger: Logger
    val entityProvider: EntityProvider
    val entityRemoveProvider: EntityRemove
    val componentProvider: ComponentProvider

    val read: EntityReadOperations
    val write: EntityMutateOperations

    val queryManager: QueryManager
    val components: Components
    val engine: Engine

    val eventRunner: EventRunner
    val pipeline: Pipeline

    val defaults: Defaults
}

