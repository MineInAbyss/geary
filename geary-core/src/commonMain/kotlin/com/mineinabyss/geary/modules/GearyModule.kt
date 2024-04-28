package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.idofront.di.DI

val geary: GearyModule by DI.observe()

fun <T : GearyModule> geary(
    moduleProvider: GearyModuleProviderWithDefault<T>,
    configuration: GearyConfiguration.() -> Unit = {}
) {
    val module = moduleProvider.default()
    geary(moduleProvider, module, configuration)
}

fun <T : GearyModule> geary(
    moduleProvider: GearyModuleProvider<T>,
    module: T,
    configuration: GearyConfiguration.() -> Unit = {}
) {
    moduleProvider.init(module)
    module.invoke(configuration)
    moduleProvider.start(module)
}

@GearyDSL
interface GearyModule {
    val logger: Logger
    val entityProvider: EntityProvider
    val componentProvider: ComponentProvider

    val read: EntityReadOperations
    val write: EntityMutateOperations

    val queryManager: QueryManager
    val components: Components
    val engine: Engine

    val eventRunner: EventRunner
    val pipeline: Pipeline

    val defaults: Defaults

    operator fun invoke(configure: GearyConfiguration.() -> Unit) {
        GearyConfiguration(this).apply(configure)
    }

    companion object
}

