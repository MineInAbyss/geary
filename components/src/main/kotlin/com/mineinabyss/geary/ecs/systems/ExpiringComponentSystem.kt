package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.Expiry
import com.mineinabyss.geary.ecs.engine.QueryResult

/**
 * Handles removing components when an [Expiry] relation exists with another component.
 */
public object ExpiringComponentSystem : TickingSystem() {
    private val QueryResult.expiry by relation<Expiry>()

    override fun QueryResult.tick() {
        if (expiry.data.timeOver()) {
            entity.remove(expiry.component.id)
            entity.remove(expiry.relation.id)
        }
    }
}
