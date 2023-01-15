package com.mineinabyss.geary.game.systems

import com.mineinabyss.geary.game.components.Expiry
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.TargetScope

/**
 * Handles removing components when an [Expiry] relation exists with another component.
 */
class ExpiringComponentSystem : RepeatingSystem() {
    private val TargetScope.expiry by getRelations<Expiry, Any?>()

    override fun TargetScope.tick() {
        if (expiry.data.timeOver()) {
            entity.remove(expiry.kind.id)
            entity.remove(expiry.relation.id)
        }
    }
}
