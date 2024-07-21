package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.ComponentId

data class ReEmitEvent(
    val findByRelationKind: SerializableComponentId,
    val dataComponentId: ComponentId,
    val data: Any?,
)

