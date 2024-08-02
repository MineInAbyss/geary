package com.mineinabyss.geary.actions.expressions

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object InlineExpressionSerializer : KSerializer<Expression<*>> {
    override val descriptor: SerialDescriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Expression<*> {
        return Expression.parseExpression(
            decoder.decodeString(),
            decoder.serializersModule
        )
    }

    override fun serialize(encoder: Encoder, value: Expression<*>) {
        TODO("Not yet implemented")
    }

}
