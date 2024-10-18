package com.mineinabyss.geary.actions.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.InlineExpressionSerializer
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable

class EvalAction(
    val expression: Expression<*>,
) : Action {
    override fun ActionGroupContext.execute() =
        expression.evaluate(this)

    class Serializer(
        world: Geary,
    ) : InnerSerializer<Expression<*>, EvalAction>(
        serialName = "geary:eval",
        inner = InlineExpressionSerializer(world),
        inverseTransform = { it.expression },
        transform = { EvalAction(it) }
    )
}
