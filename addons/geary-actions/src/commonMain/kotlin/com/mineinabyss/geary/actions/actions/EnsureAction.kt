package com.mineinabyss.geary.actions.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionsCancelledException
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.actions.event_binds.EventBind
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.SerializedComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable(with = EnsureAction.Serializer::class)
class EnsureAction(
    val conditions: SerializedComponents,
) : Action {
    @Transient
    private val flat = conditions.map { componentId(it::class) to it }

    override fun ActionGroupContext.execute() {
        flat.forEach { (id, data) ->
            when (data) {
                is Condition -> with(data) {
                    if(!execute()) throw ActionsCancelledException()
                }
                else -> entity.emit(id, data) //TODO use geary condition system if we get one
            }
        }
    }

    object Serializer: InnerSerializer<SerializedComponents, EnsureAction>(
        serialName = "geary:ensure",
        inner = PolymorphicListAsMapSerializer.ofComponents(),
        inverseTransform = { it.conditions },
        transform = { EnsureAction(it) }
    )
}
