package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.accessors.EventResultScope
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope

//TODO cancelable events
public class CheckEvent {
    public var success: Boolean = true
}

public fun GearyHandlerScope.onCheck(run: EventResultScope.(CheckEvent) -> Boolean) {
    on<CheckEvent> { check ->
        if (check.success)
            check.success = run(check)
    }
}
