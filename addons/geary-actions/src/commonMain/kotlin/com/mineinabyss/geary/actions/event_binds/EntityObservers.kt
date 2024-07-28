package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.ActionGroup
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlin.jvm.JvmInline

@Serializable(with = EntityObservers.Serializer::class)
class EntityObservers(
    val observers: List<EventBind>,
) {
    class Serializer : InnerSerializer<Map<SerializableComponentId, ActionGroup>, EntityObservers>(
        serialName = "geary:observe",
        inner = MapSerializer(
            SerializableComponentId.serializer(),
            ActionGroup.Serializer
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


@JvmInline
@Serializable
value class ActionWhen(val conditions: List<EnsureAction>)

@JvmInline
@Serializable
value class ActionRegister(val register: String)
