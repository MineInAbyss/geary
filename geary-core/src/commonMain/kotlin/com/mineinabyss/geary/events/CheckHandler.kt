package com.mineinabyss.geary.events

import com.mineinabyss.geary.components.RequestCheck
import com.mineinabyss.geary.components.events.FailedCheck
import com.mineinabyss.geary.datatypes.family.MutableFamilyOperations.Companion.has
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.EventScope
import com.mineinabyss.geary.systems.accessors.SourceScope
import com.mineinabyss.geary.systems.accessors.TargetScope

/**
 * A handler which will run a check on an event that requests one.
 */
public abstract class CheckHandler(
    parentListener: GearyListener,
    sourceNullable: Boolean
) : GearyHandler(parentListener, sourceNullable) {
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
