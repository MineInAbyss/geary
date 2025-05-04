package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A component that will add a relation to this entity with a [target], [data] pair.
 */
@Serializable
@SerialName("geary:relation")
data class RelationOnPrefab(
    val target: String,
    val data: @Polymorphic Component,
)
