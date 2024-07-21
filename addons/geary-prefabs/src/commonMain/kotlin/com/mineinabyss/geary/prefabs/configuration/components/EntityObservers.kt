package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    val using: SerializableComponentId? = null,
    val involving: List<SerializableComponentId> = listOf(),
    private val emit: List<SerializedComponents>
) {
    class CachedEvent(val componentId: ComponentId, val data: Any?)

    @Transient
    val emitEvents = emit.flatMap { emitter ->
        emitter.map { component ->
            if (using != null) ReEmitEvent(
                SerializableComponentId(using.id),
                componentId(component::class),
                component,
            ) else component
        }
    }.map { CachedEvent(componentId(it::class), it) }
}
