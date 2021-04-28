package com.mineinabyss.geary.ecs.actions.context

import com.mineinabyss.geary.ecs.api.actions.GearyAction
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.Source
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * > on.source
 *
 * Runs a list of actions on the source of the given entity.
 *
 * @param wrapped The list of actions to run.
 */
@Serializable(with = SwitchToSourceSerializer::class)
public class SwitchToSourceAction(
    override val wrapped: List<GearyAction>
) : GearyAction(), FlatWrap<List<GearyAction>> {
    override fun GearyEntity.run(): Boolean {
        val source = get<Source>()?.entity ?: return false

        return wrapped.count { it.runOn(source) } != 0
    }
}

public object SwitchToSourceSerializer : FlatSerializer<SwitchToSourceAction, List<GearyAction>>(
    "on.source", serializer(), { SwitchToSourceAction(it) }
)
