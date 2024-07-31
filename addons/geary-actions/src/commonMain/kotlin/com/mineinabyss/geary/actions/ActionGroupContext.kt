package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.datatypes.GearyEntity

class ActionGroupContext(
    var entity: GearyEntity,
) {
    val environment: MutableMap<String, Any?> = mutableMapOf()

    fun <T> eval(expression: Expression<T>): T = expression.evaluate(this)

    fun register(name: String, value: Any?) {
        environment[name] = value
    }

    fun copy(): ActionGroupContext {
        val newContext = ActionGroupContext(entity)
        newContext.environment.putAll(environment)
        return newContext
    }
}
