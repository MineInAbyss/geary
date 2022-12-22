package com.mineinabyss.geary.modules

import com.mineinabyss.ding.DI
import com.mineinabyss.ding.DIContext
import com.mineinabyss.geary.addons.dsl.GearyAddon
import com.mineinabyss.geary.addons.dsl.GearyDSLMarker
import com.mineinabyss.geary.engine.*
import java.util.logging.Logger

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
    fun <T : GearyAddon<A>, A> install(addon: T, run: A.() -> Unit = {})

//    operator fun invoke(configure: GearyModule.() -> Unit)
}

fun geary(configure: GearyModule.() -> Unit) {
}
