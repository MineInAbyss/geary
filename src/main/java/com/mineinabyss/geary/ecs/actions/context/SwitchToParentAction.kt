package com.mineinabyss.geary.ecs.actions.context

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Executes actions on this entity's parent
 *
 * @param run The actions to run on the parent.
 */
@Serializable(with = SwitchToParentSerializer::class)
public class SwitchToParentAction(
    override val wrapped: List<GearyAction>
) : GearyAction(), FlatWrap<List<GearyAction>> {
    override fun runOn(entity: GearyEntity): Boolean {
        val parent = entity.parent ?: return false

        return wrapped.count { it.runOn(parent) } != 0
    }
}

public object SwitchToParentSerializer : FlatSerializer<SwitchToParentAction, List<GearyAction>>(
    "on.parent", serializer(), { SwitchToParentAction(it) }
)
