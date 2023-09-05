package com.mineinabyss.geary.game.systems

import com.mineinabyss.geary.datatypes.UnsafeAccessors
import com.mineinabyss.geary.game.components.Expiry
import com.mineinabyss.geary.systems.RepeatingSystem
import com.mineinabyss.geary.systems.accessors.Pointer


/**
 * Handles removing components when an [Expiry] relation exists with another component.
 */
class ExpiringComponentSystem : RepeatingSystem() {
    private val Pointer.expiry by getRelationsWithData<Expiry, Any?>()

    @OptIn(UnsafeAccessors::class)
    override fun Pointer.tick() {
        expiry.forEach {
            if (it.data.timeOver()) {
                entity.remove(it.kind.id)
                entity.remove(it.relation.id)
            }
        }
    }
}
