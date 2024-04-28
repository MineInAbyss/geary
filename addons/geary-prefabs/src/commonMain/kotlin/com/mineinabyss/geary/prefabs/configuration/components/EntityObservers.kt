package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = EntityObservers.Serializer::class)
class EntityObservers(val observers: List<EventBind>) {
    class Serializer : InnerSerializer<List<EventBind>, EntityObservers>(
        serialName = "geary:observe",
        inner = ListSerializer(EventBind.serializer()),
        inverseTransform = { it.observers },
        transform = ::EntityObservers
    )
}

@Serializable
class EventBind(
    val event: SerializableComponentId,
    val involving: List<SerializableComponentId> = listOf(),
    val emit: SerializedComponents
)
