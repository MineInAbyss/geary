package com.mineinabyss.geary.engine

import com.mineinabyss.geary.datatypes.Entity

public interface EventRunner {
    /** Calls an event on [target] with data in an [event] entity, optionally with a [source] entity. */
    public fun callEvent(target: Entity, event: Entity, source: Entity?)
}
