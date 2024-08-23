package com.mineinabyss.geary.actions.actions

import com.mineinabyss.geary.actions.*
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
                    if (!execute()) {
                        throw ActionsCancelledException()
                    }
                }

                else -> entity?.emit(id, data) //TODO use geary condition system if we get one
            }
        }
    }

    fun conditionsMet(context: ActionGroupContext): Boolean {
        try {
            execute(context)
        } catch (e: ActionsCancelledException) {
            return false
        }
        return true
    }

    object Serializer : InnerSerializer<SerializedComponents, EnsureAction>(
        serialName = "geary:ensure",
        inner = PolymorphicListAsMapSerializer.ofComponents(),
        inverseTransform = { it.conditions },
        transform = { EnsureAction(it) }
    )
}
