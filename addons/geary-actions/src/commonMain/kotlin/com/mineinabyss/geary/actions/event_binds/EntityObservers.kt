package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.ActionGroup
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import kotlinx.serialization.Contextual
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.jvm.JvmInline

@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = ContextualSerializer::class)
class EntityObservers(
    val observers: List<EventBind>,
) {
    class Serializer(
        world: Geary
    ) : InnerSerializer<Map<SerializableComponentId, ActionGroup>, EntityObservers>(
        serialName = "geary:observe",
        inner = MapSerializer(
            SerializableComponentId.serializer(),
            ActionGroup.Serializer(world)
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

class ActionOnFail(val action: ActionGroup) {
    class Serializer(
        world: Geary
    ) : InnerSerializer<ActionGroup, ActionOnFail>(
        serialName = "geary:on_fail",
        inner = ActionGroup.Serializer(world),
        inverseTransform = ActionOnFail::action,
        transform = { ActionOnFail(it) }
    )
}

@JvmInline
@Serializable
value class ActionLoop(val expression: String)

class ActionEnvironment(val environment: Map<String, Expression<@Contextual Any>>) {
    class Serializer(
        world: Geary,
    ) : InnerSerializer<Map<String, Expression<@Contextual Any>>, ActionEnvironment>(
        serialName = "geary:with",
        inner = MapSerializer(String.serializer(), Expression.Serializer(world, ContextualSerializer(Any::class))),
        inverseTransform = ActionEnvironment::environment,
        transform = { ActionEnvironment(it) }
    )
}
