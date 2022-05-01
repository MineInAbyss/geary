package com.mineinabyss.geary.papermc.store

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyType

public data class DecodedEntityData(
    public val persistingComponents: Set<GearyComponent>,
    public val type: GearyType,
)
