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
public class InheritPrefabs(
    public val from: Set<PrefabKey>
)
