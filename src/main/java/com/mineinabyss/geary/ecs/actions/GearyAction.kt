package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import kotlinx.serialization.Serializable

@Serializable
public abstract class GearyAction {
    public abstract fun runOn(entity: GearyEntity)
}

