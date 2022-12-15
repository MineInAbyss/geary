package com.mineinabyss.geary.context

import com.mineinabyss.ding.DI
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.serialization.IFormats
import com.mineinabyss.geary.serialization.Serializers
import java.util.logging.Logger

val geary: GearyModule by DI.observe()

interface GearyModule {
    val logger: Logger
    val entityProvider: EntityProvider
    val systems: SystemProvider
    val componentProvider: ComponentProvider

    val read: EntityReadOperations
    val write: EntityMutateOperations

    val queryManager: QueryManager
    val components: Components
    val serializers: Serializers
    val formats: IFormats
    val engine: Engine

    val eventRunner: EventRunner

    fun inject()
}
