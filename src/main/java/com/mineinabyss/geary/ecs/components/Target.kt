package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity

@AutoscanComponent
public data class Target(val entity : GearyEntity)
