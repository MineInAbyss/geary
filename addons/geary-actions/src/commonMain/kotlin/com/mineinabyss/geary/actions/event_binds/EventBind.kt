package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.ActionGroup
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId

class EventBind(
    val event: SerializableComponentId,
    val involving: List<SerializableComponentId> = listOf(),
    val actionGroup: ActionGroup,
)
