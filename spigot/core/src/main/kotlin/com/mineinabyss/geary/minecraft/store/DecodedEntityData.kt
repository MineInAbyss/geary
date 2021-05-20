package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyType

public data class DecodedEntityData(
    public val persistingComponents: Set<GearyComponent>,
    public val type: GearyType,
)
