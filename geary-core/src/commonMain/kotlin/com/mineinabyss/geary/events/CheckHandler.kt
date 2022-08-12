package com.mineinabyss.geary.events

import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.systems.Listener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope

/**
 * A handler which will run a check on an event that requests one.
 */
public abstract class CheckHandler(
    parentListener: Listener,
    sourceNullable: Boolean
) : Handler(parentListener, sourceNullable) {
    init {
        parentListener.event._family.has<RequestCheck>()
    }

    public abstract fun check(source: SourceScope?, target: TargetScope, event: EventScope): Boolean

    override fun handle(source: SourceScope?, target: TargetScope, event: EventScope) {
        if (!check(source, target, event)) event.entity.apply {
            remove<RequestCheck>()
            set(FailedCheck)
        }
    }
}
