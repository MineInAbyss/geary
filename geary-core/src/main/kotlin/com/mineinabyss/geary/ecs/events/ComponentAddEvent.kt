package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.systems.EventRunner
import com.mineinabyss.geary.ecs.api.systems.GearyHandlerScope

/**
 * Called every time an entity gets all of a listener's requested components added to it.
 *
 * For example, if listening to `A`, `B`, `C`, the event triggers when any of the components is added,
 * as long as all three are present after the addition.
 *
 * MAKE SURE YOUR LISTENER DOES NOT HAVE ANY LOGIC BRANCHES. Only the top level list of components are
 * guaranteed to match, nothing else is checked.
 */
public class ComponentAddEvent(
    public val component: GearyComponentId
)

public fun GearyHandlerScope.onComponentAdd(run: EventRunner<ComponentAddEvent>) {
    val components = listener.family.components
    on<ComponentAddEvent> { componentAdded ->
        if (componentAdded.component in components)
            run(componentAdded)
    }
}
