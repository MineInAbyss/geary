package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity

public abstract class SpawnGearyEntityAction: GearyAction() {
    public abstract fun spawnEntity(): GearyEntity
}
