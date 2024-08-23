package com.mineinabyss.geary.actions

import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.datatypes.GearyEntity

class ActionGroupContext() {
    constructor(entity: GearyEntity) : this() {
        this.entity = entity
    }

    var entity: GearyEntity?
        get() = environment["entity"] as? GearyEntity
        set(value) {
            environment["entity"] = value
        }

    val environment: MutableMap<String, Any?> = mutableMapOf()

    fun <T> eval(expression: Expression<T>): T = expression.evaluate(this)

    fun register(name: String, value: Any?) {
        environment[name] = value
    }

    fun copy(): ActionGroupContext {
        val newContext = ActionGroupContext()
        newContext.environment.putAll(environment)
        return newContext
    }

    fun plus(newEnvironment: Map<String, Any?>): ActionGroupContext {
        val newContext = copy()
        newContext.environment.putAll(newEnvironment)
        return newContext
    }

    companion object
}
