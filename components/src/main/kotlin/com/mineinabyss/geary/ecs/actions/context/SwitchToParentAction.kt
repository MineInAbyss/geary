package com.mineinabyss.geary.ecs.actions.context

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * > on.parent
 *
 * Runs a list of actions on the parent of the given entity.
 *
 * @param wrapped The list of actions to run.
 */
@Serializable(with = SwitchToParentSerializer::class)
public class SwitchToParentAction(
    override val wrapped: List<GearyAction>
) : GearyAction(), FlatWrap<List<GearyAction>> {
    override fun GearyEntity.run(): Boolean {
        val parent = parent ?: return false

        return wrapped.count { it.runOn(parent) } != 0
    }
}

public object SwitchToParentSerializer : FlatSerializer<SwitchToParentAction, List<GearyAction>>(
    "on.parent", serializer(), { SwitchToParentAction(it) }
)
