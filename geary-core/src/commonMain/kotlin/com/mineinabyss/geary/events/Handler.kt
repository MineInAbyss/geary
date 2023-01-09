package com.mineinabyss.geary.events

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.RawAccessorDataScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope

/**
 * Generated within a [Listener]. Will handle events matching specified components on source/target/event entities.
 */
abstract class Handler(
    val parentListener: Listener,
    val sourceNullable: Boolean,
) {
    private val logger get() = geary.logger

    /** Runs when a matching event is fired. */
    abstract fun handle(source: SourceScope?, target: TargetScope, event: EventScope)

    /** Reads necessary data and iterates over combinations as appropriate, calling the [handle] function on each. */
    open fun processAndHandle(
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
            logger.e("Failed to run event ${parentListener::class.simpleName}")
            e.printStackTrace()
        }
    }
}


