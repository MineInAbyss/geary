package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.autoscan.AutoscanComponent
import kotlinx.serialization.Serializable

@Serializable
@AutoscanComponent
public data class Source(val entity : GearyEntity)
