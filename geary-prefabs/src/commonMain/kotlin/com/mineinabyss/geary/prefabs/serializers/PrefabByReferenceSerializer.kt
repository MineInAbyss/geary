package com.mineinabyss.geary.prefabs.serializers

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.engine.Engine
import com.mineinabyss.geary.helpers.DescriptorWrapper
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.prefabs.prefabs
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Allows us to serialize entity types to a reference to ones actually registered in the system.
 * This is used to load the static entity type when we decode components from an in-game entity.
 */
@Deprecated("This will not work properly until ktx.serialization fully supports inline classes")
class PrefabByReferenceSerializer : KSerializer<Entity> {
    private val prefabManager: PrefabManager get() = prefabs.manager
    private val engine: Engine get() = geary.engine

    override val descriptor: SerialDescriptor = DescriptorWrapper("geary:prefab", PrefabKey.serializer().descriptor)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): Entity {
        val prefabKey = decoder.decodeSerializableValue(PrefabKey.serializer())
        return (prefabManager[prefabKey] ?: error("Error deserializing, $prefabKey is not a registered prefab"))
    }

    override fun serialize(encoder: Encoder, value: Entity) {
        encoder.encodeSerializableValue(
            PrefabKey.serializer(),
            value.get<PrefabKey>() ?: error("Could not encode prefab entity without a prefab key component")
        )
    }
}
