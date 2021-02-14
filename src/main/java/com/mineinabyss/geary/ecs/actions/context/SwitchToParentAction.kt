package com.mineinabyss.geary.ecs.actions.context

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.components.parent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Executes actions on this entity's parent
 *
 * @param run The actions to run on the parent.
 */
@Serializable
@SerialName("on.parent")
public class SwitchToParentAction(
    public val run: List<GearyAction>
) : GearyAction() {
    override fun runOn(entity: GearyEntity): Boolean {
        val parent = entity.parent ?: return false

        return run.count { it.runOn(parent) } != 0
    }
}
