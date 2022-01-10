package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("geary:child")
public value class ChildOnPrefab(
//    public val get: String,
//    @Serializable(with = GearyEntitySerializer::class)
    public val new: List<@Polymorphic GearyComponent> //GearyEntity//? = null
)
