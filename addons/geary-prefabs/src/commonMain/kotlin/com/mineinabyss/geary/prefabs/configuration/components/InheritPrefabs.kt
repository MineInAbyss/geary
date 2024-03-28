package com.mineinabyss.geary.prefabs.configuration.components

import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer

/**
 * > geary:inherit
 *
 * Will make this entity an instance of all entities defined in [from]
 */
@Serializable(with = InheritPrefabs.Serializer::class)
class InheritPrefabs(
    val from: Set<PrefabKey>
) {
    class Serializer : InnerSerializer<Set<PrefabKey>, InheritPrefabs>(
        "geary:inherit",
        SetSerializer(PrefabKey.serializer()),
        { InheritPrefabs(it) },
        { it.from }
    )
}
