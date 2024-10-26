package com.mineinabyss.geary.actions.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary

class EmitEventAction(
    val eventId: ComponentId,
    val data: Any?,
) : Action {
    override fun ActionGroupContext.execute() {
        entity?.emit(event = eventId, data = data)
    }

    companion object {
        fun from(world: Geary, data: Any) = EmitEventAction(world.componentId(data::class), data)

        fun wrapIfNotAction(world: Geary, data: Any) = if (data is Action) data else from(world, data)
    }
}
