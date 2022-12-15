package com.mineinabyss.geary.context

import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.serialization.IFormats
import com.mineinabyss.geary.serialization.Serializers
import java.util.logging.Logger

public interface GearyModule {
    public val logger: Logger
    public val entityProvider: EntityProvider
    public val systems: SystemProvider
    public val componentProvider: ComponentProvider

    public val read: EntityReadOperations
    public val write: EntityMutateOperations

    public val queryManager: QueryManager
    public val components: Components
    public val serializers: Serializers
    public val formats: IFormats
    public val engine: Engine

    public val eventRunner: EventRunner
}

interface TransitiveModule {
    val submodules: List<Any>
}
