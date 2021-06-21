package com.mineinabyss.geary.ecs.systems

import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.PassiveActionsComponent
import com.mineinabyss.geary.ecs.components.PassiveActionsDisabledComponent
import com.mineinabyss.geary.ecs.engine.QueryResult

/**
 * A system that runs actions from [PassiveActionsComponent]s every 20 ticks.
 */
public object PassiveActionsSystem : TickingSystem(interval = 20) {
    private val QueryResult.actions by get<PassiveActionsComponent>()
    private val passivesDisabled = lacks<PassiveActionsDisabledComponent>()
    private val passivesDisabledWithData = lacks<PassiveActionsDisabledComponent>(set = true)

    override fun QueryResult.tick() {
        actions.wrapped.forEach {
            it.runOn(entity)
        }
    }
}
