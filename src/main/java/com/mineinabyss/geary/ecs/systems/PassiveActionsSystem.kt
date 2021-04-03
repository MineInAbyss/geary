package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.PassiveActionsComponent

/**
 * A system that runs actions every 20 ticks.
 */
public object PassiveActionsSystem : TickingSystem(interval = 20) {
    private val actions by get<PassiveActionsComponent>()

    override fun GearyEntity.tick() {
        actions.wrapped.forEach {
            it.runOn(this)
        }
    }
}
