package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.*
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
    public abstract fun handle(source: SourceScope?, target: TargetScope, event: EventScope)

    /** Reads necessary data and iterates over combinations as appropriate, calling the [handle] function on each. */
    public open fun processAndHandle(
        sourceScope: RawAccessorDataScope?,
        targetScope: RawAccessorDataScope,
        eventScope: RawAccessorDataScope,
    ) {
        if(!sourceNullable && sourceScope == null) return
        try {
            // Get iterator or empty if scope was null
            val sourceIterator = if (sourceScope == null) listOf<List<*>?>(null).iterator()
            else parentListener.source.iteratorFor(sourceScope)
            val targetIterator = //if (targetScope == null) listOf<List<*>?>(null).iterator()
            /*else*/ parentListener.target.iteratorFor(targetScope)
            val eventIterator = //if (eventScope == null) listOf<List<*>?>(null).iterator()
            /*else*/ parentListener.event.iteratorFor(eventScope)

            // Handle all combinations of data as needed
            for (eventData in eventIterator) for (sourceData in sourceIterator) for (targetData in targetIterator) {
                val sourceResult = if (sourceScope == null) null else SourceScope(sourceScope.entity, sourceData!!)
                val targetResult = /*if (targetScope == null) null else*/ TargetScope(targetScope.entity, targetData)
                val eventResult = /*if (eventScope == null) null else*/ EventScope(eventScope.entity, eventData)
                handle(sourceResult, targetResult, eventResult)
            }
        } catch (e: Exception) {
            logError("Failed to run event ${parentListener::class.simpleName}")
            e.printStackTrace()
        }
    }
}


