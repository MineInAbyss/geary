package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.GearyComponent
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A component that will add a relation to this entity with a [key], [value] pair.
 */
@Serializable
@SerialName("geary:relation")
public data class RelationOnPrefab(
    val key: String,
    val value: @Polymorphic GearyComponent,
)
