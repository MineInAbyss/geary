package com.mineinabyss.geary.actions.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.EntityExpression
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = BecomeAction.Serializer::class)
@SerialName("geary:become")
class BecomeAction(
    val become: EntityExpression,
) : Action {
    override fun ActionGroupContext.execute() {
        entity = become.evaluate(this)
    }

    object Serializer : InnerSerializer<EntityExpression, BecomeAction>(
        serialName = "geary:become",
        inner = EntityExpression.serializer(),
        inverseTransform = { it.become },
        transform = { BecomeAction(it) }
    )
}
