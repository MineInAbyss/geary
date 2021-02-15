package com.mineinabyss.geary.ecs.actions.context

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.components.Source
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Used to execute actions on a different [GearyEntity].
 *
 * @param run The actions to run on the other [GearyEntity].
 */
@Serializable(with = SwitchToSourceSerializer::class)
public class SwitchToSourceAction(
    override val wrapped: List<GearyAction>
) : GearyAction(), FlatWrap<List<GearyAction>> {
    override fun runOn(entity: GearyEntity): Boolean {
        val source = entity.get<Source>()?.entity ?: return false

        return wrapped.count{it.runOn(source)} != 0
    }
}

public object SwitchToSourceSerializer : FlatSerializer<SwitchToSourceAction, List<GearyAction>>(
    "on.source", serializer(), { SwitchToSourceAction(it) }
)
