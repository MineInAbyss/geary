package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.Application
import com.mineinabyss.geary.addons.ApplicationEnvironment
import com.mineinabyss.geary.addons.ApplicationFactory
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.idofront.di.DI

val geary: GearyModule by DI.observe()

fun <T : GearyModule, Config: Any> geary(
    moduleProvider: ApplicationFactory<T, Config>,
    configure: Config.() -> Unit = {},
    module: T.() -> Unit = {}
): T {
    val module = moduleProvider.create(configure).apply(module)
    DI.add<GearyModule>(module)
    return module
}

@GearyDSL
interface GearyModule: Application {
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

//    operator fun invoke(configure: GearyConfiguration.() -> Unit) {
//        GearyConfiguration(this).apply(configure)
//    }

    fun start()

    companion object
}

