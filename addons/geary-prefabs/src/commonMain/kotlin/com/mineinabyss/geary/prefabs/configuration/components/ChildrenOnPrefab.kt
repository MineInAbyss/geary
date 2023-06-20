package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.datatypes.Component
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * > geary:children
 *
 * A component that will add a list of named children to this entity.
 *
 * The keys will be used to set an extra [EntityName] component.
 */
@Serializable
@SerialName("geary:children")
class ChildrenOnPrefab(
    val nameToComponents: Map<String, List<@Polymorphic Component>>
)
