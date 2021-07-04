package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.Serializable

@Serializable(with = PrefabKeySerializer::class)
@JvmInline
public value class PrefabKey(public val key: String) {
    public constructor(plugin: String, name: String) : this("$plugin:$name")

    public val plugin: String get() = key.substringBefore(':')
    public val name: String get() = key.substringAfter(':')

    public fun toEntity(): GearyEntity? = PrefabManager[this]

    override fun toString(): String = "$plugin:$name"

    public companion object {
        public fun of(stringKey: String): PrefabKey {
            val split = stringKey.split(':')
            if (split.size != 2)
                error("Malformatted prefab key: $stringKey. Must only contain one : that splits namespace and key.")
            val (plugin, name) = split
            return PrefabKey(plugin, name)
        }
    }
}
