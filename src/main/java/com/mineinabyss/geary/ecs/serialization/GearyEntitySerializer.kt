package com.mineinabyss.geary.ecs.serialization

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.entity
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

public object GearyEntitySerializer : KSerializer<GearyEntity> {
    private val serializer = serializer<Set<@kotlinx.serialization.Contextual GearyComponent>>()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(serializer, value.getPersistingComponents())
    }

    override fun deserialize(decoder: Decoder): GearyEntity {
        return Engine.entity {
            setAllPersisting(decoder.decodeSerializableValue(serializer))
        }
    }
}
