package com.mineinabyss.geary.modules

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.addons.dsl.GearyDSL
import com.mineinabyss.geary.engine.*
import com.mineinabyss.idofront.di.DI

val geary: GearyModule by DI.observe()

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

    fun inject()
    fun start()

    operator fun invoke(configure: GearyConfiguration.() -> Unit) {
        GearyConfiguration().apply(configure)
    }
}

