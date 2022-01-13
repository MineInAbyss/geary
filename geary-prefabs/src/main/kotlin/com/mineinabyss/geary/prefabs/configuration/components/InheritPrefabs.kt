package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > geary:inherit
 */
@Serializable
@SerialName("geary:inherit")
public class InheritPrefabs(
    public val from: Set<PrefabKey>
)
