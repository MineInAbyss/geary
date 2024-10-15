package com.mineinabyss.geary.serialization.serializers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.serialization.getAllPersisting
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

//TODO register contextual serializer for world
typealias SerializableGearyEntity = @Contextual GearyEntity

class GearyEntitySerializer(val world: Geary) : KSerializer<GearyEntity> {
    private val componentSerializer = PolymorphicListAsMapSerializer.ofComponents()
    override val descriptor = SerialDescriptor("geary:entity", componentSerializer.descriptor)

    override fun deserialize(decoder: Decoder): GearyEntity {
        return world.entity {
            setAll(componentSerializer.deserialize(decoder))
        }
    }

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(componentSerializer, value.getAllPersisting().toList())
    }
}
