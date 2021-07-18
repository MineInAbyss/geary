package com.mineinabyss.geary.ecs.api.properties

import com.mineinabyss.geary.ecs.api.entities.GearyEntity

public abstract class EntityProperty<T : Any> : (GearyEntity) -> T? {
    final override fun invoke(p1: GearyEntity): T? = p1.read()

    public abstract fun GearyEntity.read(): T?
}
