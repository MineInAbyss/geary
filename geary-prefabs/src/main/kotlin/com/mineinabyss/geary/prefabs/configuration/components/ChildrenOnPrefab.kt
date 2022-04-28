package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.components.EntityName
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:children
 *
 * A component that will add a list of named children to this entity.
 *
 * The keys will be used to set an extra [EntityName] component.
 */
@JvmInline
@Serializable
@SerialName("geary:children")
public value class ChildrenOnPrefab(
    public val nameToComponents: Map<String, List<@Polymorphic GearyComponent>> //GearyEntity//? = null
)
