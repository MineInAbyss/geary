package com.mineinabyss.geary.ecs.serialization

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
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
    public val componentListSerializer: KSerializer<List<GearyComponent>> = ListSerializer(PolymorphicSerializer(GearyComponent::class))
    override val descriptor: SerialDescriptor = componentListSerializer.descriptor

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(componentListSerializer, value.getPersistingComponents().toList())
    }

    override fun deserialize(decoder: Decoder): GearyEntity {
        //TODO serialize and deserialize in the same way we convert from list of components to entities
        return Engine.entity {
            setAll(decoder.decodeSerializableValue(componentListSerializer))
        }
    }
}
