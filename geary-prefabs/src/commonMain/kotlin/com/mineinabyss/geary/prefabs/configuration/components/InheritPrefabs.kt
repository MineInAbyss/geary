package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:inherit
 *
 * Will make this entity an instance of all entities defined in [from]
 */
@Serializable
@SerialName("geary:inherit")
class InheritPrefabs(
    val from: Set<PrefabKey>
)
