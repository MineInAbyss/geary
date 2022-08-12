package com.mineinabyss.geary.serialization

import com.mineinabyss.geary.components.EntityName
import com.mineinabyss.geary.datatypes.Component
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.helpers.toGeary
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer

/**
 * A serializer which loads a new entity from a list of components.
 */
//context(EngineContext)
//TODO serialization of value class broken in 1.6.20
public object EntitySerializer /*: KSerializer<GearyEntity>*/ {
    public val componentListSerializer: KSerializer<List<Component>> =
        ListSerializer(PolymorphicSerializer(Component::class))
//    override val descriptor: SerialDescriptor = componentListSerializer.descriptor
//
//    override fun serialize(encoder: Encoder, value: GearyEntity) {
//            encoder.encodeSerializableValue(componentListSerializer, value.getPersistingComponents().toList())
//    }
//
//    override fun deserialize(decoder: Decoder): GearyEntity {
//            entity { setAll(decoder.decodeSerializableValue(componentListSerializer)) }
//    }
}

//TODO this should be handled within a serializer of sorts for GearyEntity
public fun Entity.parseEntity(expression: String): Entity {
    return when {
        expression.startsWith("parent") -> {
            val parent = (parent ?: error("Failed to read expression, entity had no parent: $expression"))

            if (expression.startsWith("parent."))
                parent.parseEntity(expression.removePrefix("parent."))
            else parent
        }
        expression.startsWith("child") -> {
            val childName = expression.substringAfter('(').substringBefore(')')
            val child = (children.find { it.get<EntityName>()?.name == childName }
                ?: error("No child named $childName found: $expression"))
            val childExpression = expression.substringAfter("].", missingDelimiterValue = "")

            if (childExpression != "")
                child.parseEntity(childExpression)
            else child
        }
        expression.contains(':') -> componentId(expression).toGeary()
        else -> error("Malformed expression for getting entity: $expression")
    }
}
