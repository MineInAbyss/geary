package com.mineinabyss.geary.ecs.prefab

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
public object PrefabByReferenceSerializer : KSerializer<GearyPrefab> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("geary:prefab", PrimitiveKind.STRING)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): GearyPrefab {
        val (plugin, prefab) = decoder.decodeString().split(':')
        return (PrefabManager[prefab] ?: error("Error deserializing, $plugin:$prefab is not a registered prefab"))
    }

    override fun serialize(encoder: Encoder, value: GearyPrefab) {
        encoder.encodeString(value.name)
    }
}
