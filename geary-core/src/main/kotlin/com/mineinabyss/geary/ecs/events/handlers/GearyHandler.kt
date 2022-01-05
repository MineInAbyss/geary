package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.*
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.idofront.messaging.logError

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Trying to use entity scope in event, use EventResultScope instead"
)
public annotation class WrongScope

/**
 * Placed within a [GearyListener] as an `object` or `inner class` to fire when an event matching
 * specified components gets fired on an entity matching the listener's components.
 */
public abstract class GearyHandler(
    public val parentListener: GearyListener
) {
    public abstract fun handle(source: SourceScope?, target: TargetScope?, event: EventScope?)

    /** Be sure [event] is of the same type as this listener wants! */
    public open fun runEvent(
        sourceScope: RawAccessorDataScope?,
        targetScope: RawAccessorDataScope?,
        eventScope: RawAccessorDataScope?,
    ) {
        try {
            // Get iterator or empty if scope was null
            val sourceIterator = if (sourceScope == null) listOf<List<*>>().iterator()
            else parentListener.source.iteratorFor(sourceScope)
            val targetIterator = if (targetScope == null) listOf<List<*>>().iterator()
            else parentListener.target.iteratorFor(targetScope)
            val eventIterator = if (eventScope == null) listOf<List<*>>().iterator()
            else parentListener.event.iteratorFor(eventScope)

            // Handle all combinations of data as needed
            for (eventData in eventIterator) for (sourceData in sourceIterator) for (targetData in targetIterator) {
                val sourceResult = if (sourceScope == null) null else SourceScope(sourceScope.entity, sourceData)
                val targetResult = if (targetScope == null) null else TargetScope(targetScope.entity, targetData)
                val eventResult = if (eventScope == null) null else EventScope(eventScope.entity, eventData)
                handle(sourceResult, targetResult, eventResult)
            }
        } catch (e: Exception) {
            logError("Failed to run event ${parentListener::class.simpleName}")
            e.printStackTrace()
        }
    }
}


