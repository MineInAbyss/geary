package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.Serializable

@Serializable
abstract class Expression<T> {
    abstract fun evaluate(context: ActionGroupContext): T
}
