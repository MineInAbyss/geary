package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.FailedCheck
import com.mineinabyss.geary.ecs.events.RequestCheck

/**
 * A handler which will run a check on an event that requests one.
 */
public abstract class CheckHandler(parentListener: GearyListener) : GearyHandler(parentListener) {
    init {
        parentListener.event.has<RequestCheck>()
    }

    public abstract fun check(source: SourceScope?, target: TargetScope, event: EventScope): Boolean

    override fun handle(source: SourceScope?, target: TargetScope, event: EventScope) {
        if (!check(source, target, event)) event.entity.apply {
            remove<RequestCheck>()
            set(FailedCheck)
        }
    }
}
