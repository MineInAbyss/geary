package com.mineinabyss.geary.ecs.actions.context

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.Target
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * > on.target
 *
 * Runs a list of actions on the target of the given entity.
 *
 * @param wrapped The list of actions to run.
 */
@Serializable(with = SwitchToTargetSerializer::class)
public class SwitchToTargetAction(
    override val wrapped: List<GearyAction>
) : GearyAction(), FlatWrap<List<GearyAction>> {
    override fun GearyEntity.run(): Boolean {
        val target = get<Target>()?.entity ?: return false

        return wrapped.count { it.runOn(target) } != 0
    }
}

public object SwitchToTargetSerializer : FlatSerializer<SwitchToTargetAction, List<GearyAction>>(
    "on.target", serializer(), { SwitchToTargetAction(it) }
)
