package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer

@Serializable(with = EntityObservers.Serializer::class)
class EntityObservers(
    val observers: List<EventBind>,
) {
    class Serializer : InnerSerializer<Map<SerializableComponentId, Tasks>, EntityObservers>(
        serialName = "geary:observe",
        inner = MapSerializer(
            ContextualSerializer(ComponentId::class),
            Tasks.serializer()
        ),
        inverseTransform = { TODO() },
        transform = {
            EntityObservers(
                it.map { (event, actionGroup) ->
                    EventBind(event, actionGroup = actionGroup)
                }
            )
        }
    )
}
