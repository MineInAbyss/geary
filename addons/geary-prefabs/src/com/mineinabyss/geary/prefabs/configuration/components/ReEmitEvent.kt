package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId

data class ReEmitEvent(
    val findByRelationKind: SerializableComponentId,
    val dataComponentId: ComponentId,
    val data: Any?,
)

