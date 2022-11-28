package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.context.QueryContext
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.systems.RepeatingSystem
import kotlinx.coroutines.*
import org.koin.core.component.createScope
import org.koin.core.component.inject
import org.koin.core.logger.Logger
import org.koin.core.scope.Scope
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * The default implementation of Geary's Engine.
 *
 * This engine uses [Archetype]s. Each component is an entity itself with an id associated with it.
 * We keep track of each entity's components in the form of it's [EntityType] stored in the [records].
 *
 * Learn more [here](https://github.com/MineInAbyss/Geary/wiki/Basic-ECS-engine-architecture).
 */
public open class ArchetypeEngine(override val tickDuration: Duration) : TickingEngine(), QueryContext {
    override val scope: Scope by lazy { createScope<Engine>(this) }

    protected val logger: Logger by inject()

    override val queryManager: QueryManager by inject()

    override val entityProvider: EntityProvider by inject()
    override val systems: SystemProvider by inject()

    override val read: EntityReadOperations by inject()
    override val write: EntityMutateOperations by inject()
    override val componentProvider: ComponentProvider by inject()
    override val eventRunner: EventRunner by inject()

    internal val archetypeProvider: ArchetypeProvider by inject()

//    @PublishedApi
//    internal val records: TypeMap by inject()

    override val coroutineContext: CoroutineContext =
        (CoroutineScope(Dispatchers.Default) + CoroutineName("Geary Engine")).coroutineContext

    /** Describes how to individually tick each system */
    protected open suspend fun RepeatingSystem.runSystem() {
        doTick()
    }

    public override fun scheduleSystemTicking() {
        var tick = 0L
        launch {
            while (true) {
                tick(tick++)
                delay(tickDuration)
            }
        }
    }

    override suspend fun tick(currentTick: Long): Unit = coroutineScope {
        // Create a job but don't start it
        val tickJob = launch(start = CoroutineStart.LAZY) {
            systems.getRepeatingInExecutionOrder()
                .filter { currentTick % (it.interval / tickDuration).toInt().coerceAtLeast(1) == 0L }
                .forEach {
                    runCatching {
                        it.runSystem()
                    }.onFailure {
                        logger.error("Error while running system ${it::class.simpleName}")
                        it.printStackTrace()
                    }
                }
        }

        // Tick all systems
        logger.debug("Started engine tick")
        tickJob.join()
        logger.debug("Finished engine tick")
    }
}
