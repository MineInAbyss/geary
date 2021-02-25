package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.PrefabKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Allows us to serialize entity types to a reference to ones actually registered in the system.
 * This is used to load the static entity type when we decode components from an in-game entity.
 */
public object PrefabByReferenceSerializer : KSerializer<GearyEntity> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("geary:prefab", PrimitiveKind.STRING)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): GearyEntity {
        val prefabKey = decoder.decodeSerializableValue(PrefabKey.serializer())
        return (PrefabManager[prefabKey] ?: error("Error deserializing, $prefabKey is not a registered prefab"))
    }

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(
            PrefabKey.serializer(),
            value.get<PrefabKey>() ?: error("Could not encode prefab entity without a prefab key component")
        )
    }
}
