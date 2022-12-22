package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.ding.DI
import com.mineinabyss.ding.DIContext
import com.mineinabyss.geary.addons.Namespaced
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyDSLMarker
import com.mineinabyss.geary.engine.*

val geary: GearyModule by DI.observe()

@GearyDSLMarker
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
    val addons: DIContext
    val pipeline: Pipeline

    fun inject()
    fun start()

//    operator fun invoke(configure: GearyModule.() -> Unit)
}

fun geary(configure: GearyConfiguration.() -> Unit) {
}

interface GearyConfiguration {
    fun <T : GearyAddon<Module, Conf>, Module, Conf> install(addon: T, configure: Conf.() -> Unit = {})

    fun namespace(namespace: String, configure: Namespaced.() -> Unit) = Namespaced(namespace).configure()
}
