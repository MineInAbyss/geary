package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyComponentId

public data class RelationComponent(
    val key: GearyComponentId,
    val value: GearyComponent
)
