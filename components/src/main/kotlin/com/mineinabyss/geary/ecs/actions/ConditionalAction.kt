package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * > if
 *
 * Runs a [list of actions][run] if all [conditions] are met.
 */
@Serializable
@SerialName("if")
public class ConditionalAction(
    private val conditions: List<GearyCondition>,
    private val run: List<GearyAction>
) : GearyAction() {
    override fun GearyEntity.run(): Boolean {
        if (conditions.all { it.metFor(this) }) {
            run.forEach { it.runOn(this) }
            return true
        }
        return false
    }
}

