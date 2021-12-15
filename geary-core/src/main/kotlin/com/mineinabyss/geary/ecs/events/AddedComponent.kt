package com.mineinabyss.geary.ecs.events

import com.mineinabyss.geary.ecs.api.GearyComponentId

/**
 * Called every time an entity gets all of a listener's requested components added to it.
 *
 * For example, if listening to `A`, `B`, `C`, the event triggers when any of the components is added,
 * as long as all three are present after the addition.
 *
 * MAKE SURE YOUR LISTENER DOES NOT HAVE ANY LOGIC BRANCHES. Only the top level list of components are
 * guaranteed to match, nothing else is checked.
 */
public class AddedComponent(
    public val component: GearyComponentId
)
