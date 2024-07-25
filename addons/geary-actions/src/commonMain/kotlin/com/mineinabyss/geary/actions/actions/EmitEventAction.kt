package com.mineinabyss.geary.actions.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId

class EmitEventAction(
    val eventId: ComponentId,
    val data: Any?,
): Action {
    override fun ActionGroupContext.execute() {
        entity.emit(event = eventId, data = data)
    }

    companion object {
        fun from(data: Any) = EmitEventAction(componentId(data::class), data)

        fun wrapIfNotAction(data: Any) = if(data is Action) data else from(data)
    }
}
