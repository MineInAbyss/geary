package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntityType

data class DecodedEntityData(
    val persistingComponents: Set<GearyComponent>,
    val type: GearyEntityType,
)
