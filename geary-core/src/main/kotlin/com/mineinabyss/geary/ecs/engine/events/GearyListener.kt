package com.mineinabyss.geary.ecs.engine.events

import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorHolder

public abstract class GearyListener : AccessorHolder() {
    public abstract fun run()
}
