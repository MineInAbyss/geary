package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.serialization.serializableComponents
import com.mineinabyss.geary.serialization.serializers.SerializableComponentId
import kotlinx.serialization.modules.SerializersModule

interface FunctionExpression<I, O> {
    companion object {
        fun parse(
            ref: Expression<*>,
            name: String,
            yaml: String,
            module: SerializersModule,
        ): FunctionExpressionWithInput<*, *> {
            val compClass = SerializableComponentId.Serializer.getComponent(name, module)
            val serializer = serializableComponents.serializers.getSerializerFor(compClass)
                ?: error("No serializer found for component $name")
            val expr =
                serializableComponents.formats["yml"]!!.decodeFromString<FunctionExpression<*, *>>(serializer, yaml)
            return FunctionExpressionWithInput(ref, expr)
        }
    }

    fun ActionGroupContext.map(input: I): O

    fun map(input: I, context: ActionGroupContext): O {
        return with(context) { map(input) }
    }
}
