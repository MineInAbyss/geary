package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.events.FailedCheck
import com.mineinabyss.geary.ecs.events.RequestCheck

/**
 * A handler which will run a check on an event that requests one.
 */
public abstract class CheckHandler : GearyHandler() {
    init {
        has<RequestCheck>()
    }

    public abstract fun ResultScope.check(event: EventScope): Boolean

    final override fun ResultScope.handle(event: EventScope) {
        if (!check(event)) event.entity.apply {
            remove<RequestCheck>()
            set(FailedCheck)
        }
    }
}
