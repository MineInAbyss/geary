package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
@SerialName("geary:children")
public value class ChildrenOnPrefab(
//    public val get: String,
//    @Serializable(with = GearyEntitySerializer::class)
    public val nameToComponents: Map<String, List<@Polymorphic GearyComponent>> //GearyEntity//? = null
)
