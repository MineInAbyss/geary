package com.mineinabyss.geary.ecs.api.systems

import com.mineinabyss.geary.ecs.accessors.AccessorHolder
import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.engine.Archetype
import com.mineinabyss.geary.ecs.engine.GearyEventHandler

public typealias EventRunner <T> = EventResultScope.(T) -> Unit

public abstract class GearyListener : AccessorHolder(), GearySystem {
    public abstract fun GearyHandlerScope.register()
}

public class GearyHandlerScope(
    public val archetype: Archetype,
    public val listener: GearyListener,
) {
    /** Adds an event handler that runs on any event */
    public fun handler(run: EventResultScope.() -> Unit) {
        archetype.addEventHandler(GearyEventHandler(listener, run))
    }

    /** Adds an event handler which runs when the event entity has data of type [T]. */
    public inline fun <reified T : Any> on(noinline run: EventResultScope.(T) -> Unit) {
        archetype.addEventHandler(GearyEventHandler(listener) {
            val data = event.get<T>() ?: return@GearyEventHandler
            run(data)
        })
    }
}
