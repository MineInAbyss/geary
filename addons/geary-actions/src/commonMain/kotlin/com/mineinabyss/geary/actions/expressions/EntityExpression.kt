package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.parent
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class EntityExpression(
    val expression: String,
) : Expression<GearyEntity> {
    override fun evaluate(context: ActionGroupContext): GearyEntity {
        return if (expression == "parent") context.entity.parent!!
        else Expression.Evaluate<GearyEntity>(expression).evaluate(context)
    }
}
