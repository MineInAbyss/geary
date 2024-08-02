package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.actions.ActionGroupContext

class FunctionExpressionWithInput<I, O>(
    val ref: Expression<*>,
    val expr: FunctionExpression<I, O>,
) : Expression<O> {
    override fun evaluate(context: ActionGroupContext): O {
        val input = ref.evaluate(context) as I
        return expr.map(input, context)
    }
}
