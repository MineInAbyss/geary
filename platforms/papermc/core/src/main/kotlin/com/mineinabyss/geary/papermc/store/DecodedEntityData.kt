package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyType

public data class DecodedEntityData(
    public val persistingComponents: Set<GearyComponent>,
    public val type: GearyType,
)
