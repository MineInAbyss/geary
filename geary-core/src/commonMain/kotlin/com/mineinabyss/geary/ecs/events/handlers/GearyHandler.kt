package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Logger

/**
 * Generated within a [GearyListener]. Will handle events matching specified components on source/target/event entities.
 */
public abstract class GearyHandler(
    public val parentListener: GearyListener,
    public val sourceNullable: Boolean,
) : KoinComponent {
    private val logger: Logger by inject()

    /** Runs when a matching event is fired. */
    public abstract fun handle(source: SourceScope?, target: TargetScope, event: EventScope)

    /** Reads necessary data and iterates over combinations as appropriate, calling the [handle] function on each. */
    public open fun processAndHandle(
        sourceScope: RawAccessorDataScope?,
        targetScope: RawAccessorDataScope,
        eventScope: RawAccessorDataScope,
    ) {
        if (!sourceNullable && sourceScope == null) return
        try {
            // Handle all combinations of data as needed
            parentListener.event.forEachCombination(eventScope) { eventData ->
                parentListener.target.forEachCombination(targetScope) { targetData ->
                    val eventResult = EventScope(eventScope.entity, eventData)
                    val targetResult = TargetScope(targetScope.entity, targetData)
                    if (sourceScope != null) {
                        parentListener.source.forEachCombination(sourceScope) { sourceData ->
                            val sourceResult = SourceScope(sourceScope.entity, sourceData)
                            handle(sourceResult, targetResult, eventResult)
                        }
                    } else handle(null, targetResult, eventResult)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to run event ${parentListener::class.simpleName}")
            e.printStackTrace()
        }
    }
}


