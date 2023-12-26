package com.mineinabyss.geary.prefabs.serializers

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PrefabKeySerializer : KSerializer<PrefabKey> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("geary:prefabKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PrefabKey {
        return PrefabKey.of(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: PrefabKey) {
        encoder.encodeString(value.toString())
    }
}
