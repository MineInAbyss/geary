package com.mineinabyss.geary.actions.expressions

import com.mineinabyss.geary.actions.ActionGroupContext
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.SerializersModule
import kotlin.math.min

@Serializable(with = Expression.Serializer::class)
sealed interface Expression<T> {
    fun evaluate(context: ActionGroupContext): T
    data class Fixed<T>(
        val value: T,
    ) : Expression<T> {
        override fun evaluate(context: ActionGroupContext): T = value
    }

    data class Variable<T>(
        val expression: String,
    ) : Expression<T> {
        override fun evaluate(context: ActionGroupContext): T {
            if (expression == "entity") return context.entity as T
            return context.environment[expression] as? T ?: error("Expression $expression not found in context")
        }
    }

    companion object {
        fun parseExpression(string: String, module: SerializersModule): Expression<*> {
            val (name, rem) = getFunctionName(string)
            val reference = Variable<Any>(name)
            if(rem == "") return reference
            return foldFunctions(reference, rem, module)
        }

        tailrec fun foldFunctions(
            reference: Expression<*>,
            remainder: String,
            module: SerializersModule,
        ): Expression<*> {
            val (name, afterName) = getFunctionName(remainder)
            val (yaml, afterYaml) = getYaml(afterName)
            val functionExpr = FunctionExpression.parse(reference, name, yaml, module)
            if (afterYaml == "") return functionExpr
            return foldFunctions(functionExpr, afterYaml, module)
        }

        fun getYaml(expr: String): Pair<String, String> {
            var brackets = 0
            val yamlEndIndex = expr.indexOfFirst {
                if (it == '{') brackets++
                if (it == '}') brackets--
                brackets == 0
            }
            if (yamlEndIndex <= 0) return "{}" to expr
            return expr.take(yamlEndIndex + 1).trim() to expr.drop(yamlEndIndex + 1).trim()
        }

        fun getFunctionName(expr: String): Pair<String, String> {
            val yamlStart = expr.indexOf('{').toUInt()
            val nextSection = expr.indexOf('.').toUInt()
            val end = min(yamlStart, nextSection).toInt()
            if (end == -1) return expr.trim() to ""
            return expr.take(end).trim() to expr.drop(end).removePrefix(".").trim()
        }
    }

    // TODO kaml handles contextual completely different form Json, can we somehow allow both? Otherwise
    //  kaml also has broken contextual serializer support that we need to work around :(
    class Serializer<T : Any>(val serializer: KSerializer<T>) : KSerializer<Expression<T>> {
        override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

        override fun deserialize(decoder: Decoder): Expression<T> {
            // Try reading string value, if serial type isn't string, this fails
            runCatching {
                decoder.decodeStructure(String.serializer().descriptor) {
                    decodeSerializableElement(String.serializer().descriptor, 0, String.serializer())
                }
            }.onSuccess { string ->
                if (string.startsWith("{{") && string.endsWith("}}"))
                    return parseExpression(
                        string.removePrefix("{{").removeSuffix("}}").trim(),
                        decoder.serializersModule
                    ) as Expression<T>
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

fun <T> expr(value: T) = Expression.Fixed(value)
