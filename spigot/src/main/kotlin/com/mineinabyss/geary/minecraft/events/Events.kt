package com.mineinabyss.geary.minecraft.events

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.actions.CancelEventAction
import com.mineinabyss.geary.ecs.actions.GearyAction
import com.mineinabyss.geary.ecs.components.get
import com.mineinabyss.geary.ecs.serialization.FlatSerializer
import com.mineinabyss.geary.ecs.serialization.FlatWrap
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

@Serializable(with = EventComponentSerializer::class)
public data class Events(
    override val wrapped: Map<String, List<GearyAction>>
) : GearyComponent, FlatWrap<Map<String, List<GearyAction>>>

private object EventComponentSerializer : FlatSerializer<Events, Map<String, List<GearyAction>>>(
    "geary:events", serializer(), { Events(it) }
)

public fun Event.event(entity: GearyEntity?, name: String) {
    entity?.get<Events>()?.wrapped?.get(name)?.forEach {
        it.runOn(entity)
        if (it is CancelEventAction && this is Cancellable)
            isCancelled = true
    }
}