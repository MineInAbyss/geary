package com.mineinabyss.geary.ecs.query.events

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.properties.EntityPropertyHolder
import com.mineinabyss.geary.ecs.api.systems.QueryManager

/**
 * A system that runs every time an entity has a new component added to it that makes it match this system's
 * family.
 *
 * Must call [track] to register the system.
 *
 * Currently, inherits same class as a GearyAction, but will hopefully transition to be a proper Query
 * when those get rewritten.
 */
public abstract class ComponentAddSystem : EntityPropertyHolder(), (GearyEntity) -> Unit {
    public fun track(): Unit = QueryManager.trackComponentAddSystem(this)

    public override fun invoke(entity: GearyEntity) {
        entity.runWithProperties { run() }
    }

    protected abstract fun GearyEntity.run()
}
