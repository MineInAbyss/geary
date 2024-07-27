package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.SerializersModule

@Serializable(with = Expression.Serializer::class)
sealed interface Expression<T> {
    fun evaluate(context: ActionGroupContext): T
    data class Fixed<T>(
        val value: T,
    ) : Expression<T> {
        override fun evaluate(context: ActionGroupContext): T = value
    }

    data class Evaluate<T>(
        val expression: String,
    ) : Expression<T> {
        override fun evaluate(context: ActionGroupContext): T {
            return context.environment[expression] as? T ?: error("Expression $expression not found in context")
        }
    }

    // TODO kaml handles contextual completely different form Json, can we somehow allow both? Otherwise
    //  kaml also has broken contextual serializer support that we need to work around :(
    class Serializer<T : Any>(val serializer: KSerializer<T>) : KSerializer<Expression<T>> {
        @OptIn(InternalSerializationApi::class)
        override val descriptor: SerialDescriptor =
            ContextualSerializer(Any::class).descriptor//buildSerialDescriptor("ExpressionSerializer", SerialKind.CONTEXTUAL)

        override fun deserialize(decoder: Decoder): Expression<T> {
            // Try reading string value, if serial type isn't string, this fails
            runCatching {
                decoder.decodeStructure(String.serializer().descriptor) {
                    decodeSerializableElement(String.serializer().descriptor, 0, String.serializer())
                }
            }.onSuccess { string ->
                if (string.startsWith("{{") && string.endsWith("}}"))
                    return Evaluate(string.removePrefix("{{").removeSuffix("}}").trim())
            }

            // Fallback to reading the value in-place
            return decoder.decodeStructure(serializer.descriptor) {
                Fixed(decodeSerializableElement(serializer.descriptor, 0, serializer))
            }
        }

        override fun serialize(encoder: Encoder, value: Expression<T>) {
            TODO("Not yet implemented")
        }
    }
}
