package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:relation")
public data class RelationOnPrefab(
    val key: String,
    val value: @Polymorphic GearyComponent,
) {
}
