package com.mineinabyss.geary.components

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyComponentId

public data class RelationComponent(
    val key: GearyComponentId,
    val data: GearyComponent
)
