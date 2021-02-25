package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.PassiveActionsComponent
import com.mineinabyss.geary.ecs.engine.forEach
import com.mineinabyss.geary.ecs.systems.PassiveActionsSystem.interval

/**
 * A system for the ECS that will run every [interval] ticks.
 *
 * @param interval How often to run this system in ticks.
 *
 * @see [Engine.forEach]
 */
public object PassiveActionsSystem: TickingSystem(interval = 20) {
    override fun tick() {
        Engine.forEach<PassiveActionsComponent> { (actions) ->
            actions.forEach {
                it.runOn(this)
            }
        }
    }
}
