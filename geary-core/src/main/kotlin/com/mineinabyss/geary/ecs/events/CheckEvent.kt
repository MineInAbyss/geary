package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.accessors.ResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope

//TODO cancelable events
public open class CheckEvent {
    public var success: Boolean = true
}

public fun GearyHandlerScope.onCheck(run: ResultScope.(CheckEvent) -> Boolean) {
    on<CheckEvent> { event ->
        if (!event.success)
            event.success = run(event)
    }
}
