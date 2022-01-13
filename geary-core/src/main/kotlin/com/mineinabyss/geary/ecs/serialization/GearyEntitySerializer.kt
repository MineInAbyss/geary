package com.mineinabyss.geary.ecs.serialization

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.components.EntityName
import com.mineinabyss.geary.ecs.entities.children
import com.mineinabyss.geary.ecs.entities.parent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A serializer which loads a new entity from a list of components.
 */
public object GearyEntitySerializer : KSerializer<GearyEntity> {
    public val componentListSerializer: KSerializer<List<GearyComponent>> =
        ListSerializer(PolymorphicSerializer(GearyComponent::class))
    override val descriptor: SerialDescriptor = componentListSerializer.descriptor

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(componentListSerializer, value.getPersistingComponents().toList())
    }

    override fun deserialize(decoder: Decoder): GearyEntity {
        return Engine.entity {
            setAll(decoder.decodeSerializableValue(componentListSerializer))
        }
    }
}

//TODO this should be handled within a serializer of sorts for GearyEntity
public fun GearyEntity.parseEntity(expression: String): GearyEntity {
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
