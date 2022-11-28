package com.mineinabyss.geary.engine

import com.mineinabyss.geary.context.EngineContext
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinScopeComponent

/**
 * An engine service for running the Geary ECS.
 *
 * Its companion object gets a service via Bukkit as its implementation.
 */
public interface Engine : KoinScopeComponent, EngineContext, CoroutineScope {
    override val engine: Engine get() = this

    public val entityProvider: EntityProvider
    public val systems: SystemProvider
    public val componentProvider: ComponentProvider

    public val read: EntityReadOperations
    public val write: EntityMutateOperations

    public val queryManager: QueryManager
    public val eventRunner: EventRunner
}
