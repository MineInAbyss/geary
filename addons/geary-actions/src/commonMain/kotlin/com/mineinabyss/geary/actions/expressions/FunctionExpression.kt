package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.serializers.ComponentIdSerializer
import kotlinx.serialization.modules.SerializersModule

interface FunctionExpression<I, O> {
    companion object {
        fun parse(
            world: Geary,
            ref: Expression<*>,
            name: String,
            yaml: String,
            module: SerializersModule,
        ): FunctionExpressionWithInput<*, *> {
            val serializableComponents = world.getAddon(SerializableComponents)
            val compClass = ComponentIdSerializer(serializableComponents.serializers, world).getComponent(name, module)
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
