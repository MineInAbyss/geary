package com.mineinabyss.geary.actions.event_binds

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionEntry
import com.mineinabyss.geary.actions.actions.EmitEventAction
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlin.jvm.JvmInline

@Serializable(with = EntityObservers.Serializer::class)
class EntityObservers(
    val observers: List<EventBind>,
) {
    class Serializer : InnerSerializer<Map<SerializableComponentId, List<SerializedComponents>>, EntityObservers>(
        serialName = "geary:observe",
        inner = MapSerializer(
            SerializableComponentId.serializer(),
            ListSerializer(
                PolymorphicListAsMapSerializer.ofComponents(
                    PolymorphicListAsMapSerializer.Config(
                        customKeys = mapOf(
                            "when" to ActionWhen.serializer(),
                            "register" to ActionRegister.serializer()
                        )
                    )
                )
            )
        ),
        inverseTransform = { TODO() },
        transform = {
            EntityObservers(
                it.map { (event, emit) ->
                    val actions = emit.map { components ->
                        var action: Action? = null
                        var condition: List<EnsureAction>? = null
                        var register: String? = null
                        components.forEach { comp ->
                            when {
                                comp is ActionWhen -> condition = comp.conditions
                                comp is ActionRegister -> register = comp.register
                                action != null -> error("Multiple actions defined in one block!")
                                else -> action = EmitEventAction.wrapIfNotAction(comp)
                            }
                        }
                        ActionEntry(
                            action = action!!,
                            conditions = condition,
                            register = register
                        )
                    }
                    EventBind(event, emit = actions)
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
