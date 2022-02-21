package com.mineinabyss.geary.prefabs.serializers

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.engine.EngineScope
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.serialization.DescriptorWrapper
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.PrefabManager
import com.mineinabyss.geary.prefabs.PrefabManagerScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.koin.core.component.inject

/**
 * Allows us to serialize entity types to a reference to ones actually registered in the system.
 * This is used to load the static entity type when we decode components from an in-game entity.
 */
@Deprecated("This will not work properly until ktx.serialization fully supports inline classes")
public class PrefabByReferenceSerializer : KSerializer<GearyEntity>, PrefabManagerScope, EngineScope {
    override val prefabManager: PrefabManager by inject()
    override val engine: Engine by inject()
    override val descriptor: SerialDescriptor = DescriptorWrapper("geary:prefab", PrefabKey.serializer().descriptor)

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): GearyEntity {
        val prefabKey = decoder.decodeSerializableValue(PrefabKey.serializer())
        return (prefabManager[prefabKey] ?: error("Error deserializing, $prefabKey is not a registered prefab"))
    }

    override fun serialize(encoder: Encoder, value: GearyEntity) {
        encoder.encodeSerializableValue(
            PrefabKey.serializer(),
            value.get<PrefabKey>() ?: error("Could not encode prefab entity without a prefab key component")
        )
    }
}
