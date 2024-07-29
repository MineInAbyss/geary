package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.actions.EmitEventAction
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.actions.event_binds.ActionOnFail
import com.mineinabyss.geary.actions.event_binds.ActionRegister
import com.mineinabyss.geary.actions.event_binds.ActionWhen
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

class ActionEntry(
    val action: Action,
    val conditions: List<EnsureAction>?,
    val register: String?,
    val onFail: ActionGroup?,
)

@Serializable(with = ActionGroup.Serializer::class)
class ActionGroup(
    val actions: List<ActionEntry>,
) {
    fun execute(context: ActionGroupContext) {
        actions.forEach { entry ->
            try {
                entry.conditions?.forEach { condition ->
                    with(condition) { context.execute() }
                }

                val returned = with(entry.action) { context.execute() }

                if (entry.register != null)
                    context.register(entry.register, returned)
            } catch (e: ActionsCancelledException) {
                entry.onFail?.execute(context)
                return
            }
        }
    }

    class Serializer : InnerSerializer<List<SerializedComponents>, ActionGroup>(
        serialName = "geary:action_group",
        inner = ListSerializer(
            PolymorphicListAsMapSerializer.ofComponents(
                PolymorphicListAsMapSerializer.Config(
                    customKeys = mapOf(
                        "when" to { ActionWhen.serializer() },
                        "register" to { ActionRegister.serializer() },
                        "onFail" to { ActionOnFail.serializer() }
                    )
                )
            )
        ),
        inverseTransform = { TODO() },
        transform = {
            val actions = it.mapNotNull { components ->
                var action: Action? = null
                var condition: List<EnsureAction>? = null
                var register: String? = null
                var onFail: ActionGroup? = null
                components.forEach { comp ->
                    when {
                        comp is ActionWhen -> condition = comp.conditions
                        comp is ActionRegister -> register = comp.register
                        comp is ActionOnFail -> onFail = comp.action
                        action != null -> geary.logger.w { "Multiple actions defined in one block!" }
                        else -> action = EmitEventAction.wrapIfNotAction(comp)
                    }
                }
                if (action == null) return@mapNotNull null
                ActionEntry(
                    action = action!!,
                    conditions = condition,
                    register = register,
                    onFail = onFail
                )
            }
            ActionGroup(actions)
        }
    )
}
