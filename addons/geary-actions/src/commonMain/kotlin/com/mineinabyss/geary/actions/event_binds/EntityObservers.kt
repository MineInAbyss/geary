package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.ActionGroup
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
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
            ActionGroup.serializer()
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


@Serializable(with = ActionWhen.Serializer::class)
class ActionWhen(val conditions: List<EnsureAction>) {
    class Serializer : InnerSerializer<List<EnsureAction>, ActionWhen>(
        serialName = "geary:when",
        inner = ListSerializer(EnsureAction.serializer()),
        inverseTransform = ActionWhen::conditions,
        transform = { ActionWhen(it) }
    )
}

@JvmInline
@Serializable
value class ActionRegister(val register: String)

@Serializable(with = ActionOnFail.Serializer::class)
class ActionOnFail(val action: ActionGroup) {
    class Serializer : InnerSerializer<ActionGroup, ActionOnFail>(
        serialName = "geary:on_fail",
        inner = ActionGroup.Serializer(),
        inverseTransform = ActionOnFail::action,
        transform = { ActionOnFail(it) }
    )
}

@JvmInline
@Serializable
value class ActionLoop(val expression: String)
