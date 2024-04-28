package com.mineinabyss.geary.serialization.serializers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.serialization.getAllPersisting
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializableGearyEntity = @Serializable(with = GearyEntitySerializer::class) GearyEntity


object GearyEntitySerializer : KSerializer<GearyEntity> {
    private val componentSerializer = PolymorphicListAsMapSerializer.ofComponents()
    override val descriptor = SerialDescriptor("geary:entity", componentSerializer.descriptor)

    override fun deserialize(decoder: Decoder): GearyEntity {
        return entity {
            setAll(componentSerializer.deserialize(decoder))
        }
    }

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(componentSerializer, value.getAllPersisting().toList())
    }
}
