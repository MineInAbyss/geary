package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem

public class ExpiringComponentSystem : TickingSystem() {
//    private val expiry by trait<Expiry>()

    override fun GearyEntity.tick() {
        //TODO implement once traits are in
        /*if (expiry.trait.timeOver()) {
            removeComponent(expiry.component)
            removeComponent(expiry.trait)
        }*/
    }
}
