package com.mineinabyss.geary.prefabs.configuration.components

data class ReEmitEvent(
    val findByRelationKind: SerializableComponentId,
    val data: Any?,
)

