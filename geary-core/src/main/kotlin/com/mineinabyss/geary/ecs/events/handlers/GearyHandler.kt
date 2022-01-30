package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.RawAccessorDataScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.idofront.messaging.logError

/**
 * Generated within a [GearyListener]. Will handle events matching specified components on source/target/event entities.
 */
public abstract class GearyHandler(
    public val parentListener: GearyListener,
    public val sourceNullable: Boolean,
) {
    /** Runs when a matching event is fired. */
    public abstract suspend fun handle(source: SourceScope?, target: TargetScope, event: EventScope)

    /** Reads necessary data and iterates over combinations as appropriate, calling the [handle] function on each. */
    public open suspend fun processAndHandle(
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
            logError("Failed to run event ${parentListener::class.simpleName}")
            e.printStackTrace()
        }
    }
}


