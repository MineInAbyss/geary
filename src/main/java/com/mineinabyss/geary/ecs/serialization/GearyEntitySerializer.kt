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

public object GearyEntitySerializer : KSerializer<GearyEntity> {
    private val serializer = ListSerializer(PolymorphicSerializer(GearyComponent::class))
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(serializer, value.getPersistingComponents().toList())
    }

    override fun deserialize(decoder: Decoder): GearyEntity {
        //TODO serialize and deserialize in the same way we convert from list of components to entities
        return Engine.entity {
            setAll(decoder.decodeSerializableValue(serializer))
        }
    }
}
