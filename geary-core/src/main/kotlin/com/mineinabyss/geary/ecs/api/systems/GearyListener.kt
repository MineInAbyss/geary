package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.ExecutableEvent
import com.mineinabyss.geary.ecs.engine.iteration.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.engine.iteration.accessors.QueryResult

public abstract class GearyListener : AccessorHolder(), GearySystem {
    public abstract fun Archetype.handlers()
    public inline fun <reified T : Any> Archetype.on(noinline run: QueryResult.(T) -> Unit) {
        addEventListener(T::class, ExecutableEvent(this@GearyListener, run))
    }
}
