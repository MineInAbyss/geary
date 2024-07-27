package com.mineinabyss.geary.serialization.serializers

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ProvidedConfig(val config: PolymorphicListAsMapSerializer.Config<*>) : KSerializer<ProvidedConfig> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("PolymorphicListAsMapSerializer.Config", PolymorphicKind.SEALED)

    override fun deserialize(decoder: Decoder): ProvidedConfig {
        error("")
    }

    override fun serialize(encoder: Encoder, value: ProvidedConfig) {
        error("")
    }
}
