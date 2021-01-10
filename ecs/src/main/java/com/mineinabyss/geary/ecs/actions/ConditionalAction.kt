package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.conditions.GearyCondition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("if")
public class ConditionalAction(
    private val conditions: List<GearyCondition>,
    private val run: List<GearyAction>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        if (conditions.all { it.conditionsMet(entity) }) {
            run.forEach { it.runOn(entity) }
            return true
        }
        return false
    }
}

