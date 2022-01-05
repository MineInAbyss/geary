package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.AffectedScope
import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.events.FailedCheck
import com.mineinabyss.geary.ecs.events.RequestCheck

/**
 * A handler which will run a check on an event that requests one.
 */
public abstract class CheckHandler : GearyHandler() {
    init {
        has<RequestCheck>()
    }

    public abstract fun AffectedScope.check(event: EventScope): Boolean

    final override fun handle(source: SourceScope, target: TargetScope, event: EventScope) {
        if (!source.check(event)) event.entity.apply {
            remove<RequestCheck>()
            set(FailedCheck)
        }
    }
}
