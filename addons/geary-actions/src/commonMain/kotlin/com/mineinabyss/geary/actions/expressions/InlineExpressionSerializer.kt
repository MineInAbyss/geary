package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.modules.Geary
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class InlineExpressionSerializer(
    val world: Geary,
) : KSerializer<Expression<*>> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Expression<*> {
        return Expression.parseExpression(
            world,
            decoder.decodeString(),
            decoder.serializersModule
        )
    }

    override fun serialize(encoder: Encoder, value: Expression<*>) {
        TODO("Not yet implemented")
    }
}
