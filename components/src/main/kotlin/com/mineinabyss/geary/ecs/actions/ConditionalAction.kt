package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.conditions.GearyCondition
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("if")
public class ConditionalAction(
    private val conditions: List<GearyCondition>,
    private val run: List<GearyAction>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        if (conditions.all { it.metFor(entity) }) {
            run.forEach { it.runOn(entity) }
            return true
        }
        return false
    }
}

