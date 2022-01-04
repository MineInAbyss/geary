package com.mineinabyss.geary.ecs.events.handlers

import com.mineinabyss.geary.ecs.accessors.EventScope
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.accessors.SourceScope
import com.mineinabyss.geary.ecs.accessors.TargetScope

public abstract class GearyInteractionHandler: GearyHandler() {
    public abstract fun handle(source: SourceScope, target: TargetScope, event: EventScope)

    final override fun ResultScope.handle(event: EventScope) {
        TODO("Not yet implemented")
    }
}
