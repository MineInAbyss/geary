package com.mineinabyss.geary.ecs.components

import kotlinx.serialization.Serializable

@Serializable
public data class PrefabKey(
    public val plugin: String,
    public val name: String
)
