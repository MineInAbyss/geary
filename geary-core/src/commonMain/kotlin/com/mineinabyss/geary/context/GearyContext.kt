package com.mineinabyss.geary.context

import com.mineinabyss.geary.engine.*

public interface GearyContext :
    EngineContext,
    FormatsContext,
    SerializersContext,
    QueryContext,
    ComponentsContext {
    public val logger: Logger
    public val entityProvider: EntityProvider
    public val systems: SystemProvider
    public val componentProvider: ComponentProvider

    public val read: EntityReadOperations
    public val write: EntityMutateOperations

    public val eventRunner: EventRunner
}
