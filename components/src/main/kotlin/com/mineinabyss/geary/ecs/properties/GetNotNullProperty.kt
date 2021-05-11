package com.mineinabyss.geary.ecs.properties

import com.mineinabyss.geary.ecs.api.properties.EntityPropertyHolder
import com.mineinabyss.geary.ecs.api.entities.GearyEntity

public fun <T : Any> EntityPropertyHolder.notNull(run: GearyEntity.() -> T?): GearyEntity.() -> T? = run
