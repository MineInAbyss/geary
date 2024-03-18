package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.engine.*
import com.mineinabyss.geary.helpers.fastForEach
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.TrackedSystem
import com.mineinabyss.geary.systems.query.Query
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * The default implementation of Geary's Engine.
 *
 * This engine uses [Archetype]s. Each component is an entity itself with an id associated with it.
 * We keep track of each entity's components in the form of its [EntityType] stored in the [records].
 *
 * Learn more [here](https://github.com/MineInAbyss/Geary/wiki/Basic-ECS-engine-architecture).
 */
open class ArchetypeEngine(override val tickDuration: Duration) : TickingEngine() {
    private val pipeline get() = geary.pipeline
    private val logger get() = geary.logger

    override val coroutineContext: CoroutineContext =
        (CoroutineScope(Dispatchers.Default) + CoroutineName("Geary Engine")).coroutineContext

    /** Describes how to individually tick each system */
    protected open fun <T : Query> TrackedSystem<T>.runSystem() {
        system.onTick(runner)
    }

    override fun scheduleSystemTicking() {
        var tick = 0L
        launch {
            while (true) {
                tick(tick++)
                delay(tickDuration)
            }
        }
    }

    override fun tick(currentTick: Long) {
        // Create a job but don't start it
        pipeline.getRepeatingInExecutionOrder()
            .filter {
                it.system.interval != null
                        && (currentTick % (it.system.interval / tickDuration).toInt().coerceAtLeast(1) == 0L)
            }
            .also { logger.v("Ticking engine with systems $it") }
            .fastForEach { system ->
                runCatching {
                    system.runSystem()
                }.onFailure {
                    logger.e("Error while running system ${system.system.name}")
                    it.printStackTrace()
                }
            }
    }
}
