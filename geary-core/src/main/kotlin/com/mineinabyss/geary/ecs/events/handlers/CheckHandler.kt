package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope
import com.mineinabyss.geary.ecs.api.systems.GearyListener
import com.mineinabyss.geary.ecs.events.FailedCheck
import com.mineinabyss.geary.ecs.events.RequestCheck
import kotlinx.coroutines.runBlocking

/**
 * A handler which will run a check on an event that requests one.
 */
public abstract class CheckHandler(
    parentListener: GearyListener,
    sourceNullable: Boolean
) : GearyHandler(parentListener, sourceNullable) {
    init {
        runBlocking {
            parentListener.event.has<RequestCheck>()
        }
    }

    public abstract suspend fun check(source: SourceScope?, target: TargetScope, event: EventScope): Boolean

    override suspend fun handle(source: SourceScope?, target: TargetScope, event: EventScope) {
        if (!check(source, target, event)) event.entity.apply {
            remove<RequestCheck>()
            set(FailedCheck)
        }
    }
}
