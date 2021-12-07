package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.prefab.PrefabKey
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
