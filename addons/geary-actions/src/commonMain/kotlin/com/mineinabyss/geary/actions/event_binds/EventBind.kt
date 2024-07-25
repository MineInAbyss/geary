package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import com.mineinabyss.geary.serialization.serializers.SerializedComponents

class EventBind(
    val event: SerializableComponentId,
    val involving: List<SerializableComponentId> = listOf(),
    val emit: List<SerializedComponents>,
)
