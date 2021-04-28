package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.api.autoscan.AutoscanComponent
import com.mineinabyss.geary.ecs.api.entities.GearyEntity

/**
 * Specifies a source acting on this entity during an event.
 */
@AutoscanComponent
public data class Source(val entity : GearyEntity)
