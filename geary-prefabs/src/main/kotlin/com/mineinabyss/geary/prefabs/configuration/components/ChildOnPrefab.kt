package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:child
 *
 * A component that adds a child entity to this entity from the components defined in [components]
 */
@JvmInline
@Serializable
@SerialName("geary:child")
value class ChildOnPrefab(
    val components: List<@Polymorphic Component>
)
