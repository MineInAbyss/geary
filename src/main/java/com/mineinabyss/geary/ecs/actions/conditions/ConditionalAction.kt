package com.mineinabyss.geary.ecs.actions

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.conditions.Condition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("if")
public class ConditionalAction(
    private val condition: Condition,
    private val run: List<GearyAction>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        if (condition.conditionsMet(entity)) {
            run.forEach { it.runOn(entity) }
            return true
        }
        return false
    }
}

