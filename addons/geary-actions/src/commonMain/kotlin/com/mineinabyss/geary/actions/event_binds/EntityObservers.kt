package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer

@Serializable(with = EntityObservers.Serializer::class)
class EntityObservers(val observers: List<EventBind>) {
    class Serializer : InnerSerializer<Map<SerializableComponentId, List<SerializedComponents>>, EntityObservers>(
        serialName = "geary:observe",
        inner = MapSerializer(
            SerializableComponentId.serializer(),
            ListSerializer(PolymorphicListAsMapSerializer.ofComponents())
        ),
        inverseTransform = { it.observers.associate { it.event to it.emit } },
        transform = { EntityObservers(it.map { (event, emit) -> EventBind(event, emit = emit) }) }
    )
}


