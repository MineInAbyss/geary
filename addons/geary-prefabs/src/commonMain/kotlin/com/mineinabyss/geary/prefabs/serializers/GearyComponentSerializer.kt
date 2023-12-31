package com.mineinabyss.geary.prefabs.serializers

import com.mineinabyss.geary.datatypes.GearyComponent
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class GearyComponentSerializer : KSerializer<GearyComponent> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("GearyComponentSerializer", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): GearyComponent {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: GearyComponent) {
        TODO("Not yet implemented")
    }

}
