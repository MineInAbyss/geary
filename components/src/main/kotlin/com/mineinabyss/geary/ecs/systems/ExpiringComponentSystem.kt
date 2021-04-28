package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.Expiry

/**
 * Handles removing components when an [Expiry] relation exists with another component.
 */
public object ExpiringComponentSystem : TickingSystem() {
    private val expiry by relation<Expiry>()

    override fun GearyEntity.tick() {
        if (expiry.data.timeOver()) {
            remove(expiry.component.id)
            remove(expiry.relation.id)
        }
    }
}
