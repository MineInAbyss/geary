package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.context.geary
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.prefabs.serializers.PrefabKeySerializer
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

/**
 * An inline class which represents a key build from a [namespace] and [key], separated
 * by a '`:`' symbol.
 */
@Serializable(with = PrefabKeySerializer::class)
// We don't make this a value class since calculating substring is pretty expensive compared to one new object instantiation
data class PrefabKey private constructor(val namespace: String, val key: String) : KoinComponent {
    fun toEntity(): Entity = toEntityOrNull()
        ?: error("Requested non null prefab entity for $this, but it does not exist.")

    fun toEntityOrNull(): Entity? = geary.prefabManager[this]

    val full get() = "$namespace:$key"

    override fun toString(): String = full

    companion object {
        /** Creates a key like [of] but returns null when parsing fails. */
        fun ofOrNull(stringKey: String): PrefabKey? = runCatching { of(stringKey) }.getOrNull()

        /** Creates a key from a string with [namespace] and [key] separated by one '`:`' character. */
        fun of(stringKey: String): PrefabKey {
            val split = stringKey.split(':')
            if (split.size != 2)
                error("Malformed prefab key: $stringKey. Must only contain one : that splits namespace and key.")
            return PrefabKey(split[0], split[1])
        }

        /** Creates a key from a [namespace] and [name] which must not contain any '`:`' characters. */
        fun of(namespace: String, name: String): PrefabKey = of("$namespace:$name")
    }
}

