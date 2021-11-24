package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.GearyEventHandler

public typealias EventRunner <T> = ResultScope.(T) -> Unit

public abstract class GearyListener(engine: GearyEngine) : AccessorHolder(engine), GearySystem {
    public abstract fun GearyHandlerScope.register()
}

public class GearyHandlerScope(
    public val archetype: Archetype,
    public val listener: GearyListener,
) {
    public inline fun <reified T : Any> on(noinline run: EventRunner<T>) {
        archetype.addEventHandler(T::class, GearyEventHandler(listener, run))
    }
}
