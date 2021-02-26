package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.api.systems.accessor
import com.mineinabyss.geary.ecs.components.PassiveActionsComponent
import com.mineinabyss.geary.ecs.systems.PassiveActionsSystem.interval

/**
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [Engine.forEach]
 */
public object PassiveActionsSystem : TickingSystem(interval = 20) {
    private val actions by accessor<PassiveActionsComponent>()

    override fun GearyEntity.tick() {
        actions.wrapped.forEach {
            it.runOn(this)
        }
    }
}
