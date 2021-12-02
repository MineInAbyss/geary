package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import kotlinx.serialization.Serializable

/**
 * An inline class which represents a key build from a [namespace] and [name], separated
 * by a '`:`' symbol.
 */
@Serializable(with = PrefabKeySerializer::class)
@JvmInline
public value class PrefabKey private constructor(public val key: String) {
    public val namespace: String get() = key.substringBefore(':')
    public val name: String get() = key.substringAfter(':')

    override fun toString(): String = "$namespace:$name"

    public companion object {
        /** Creates a key like [of] but returns null when parsing fails. */
        public fun ofOrNull(stringKey: String): PrefabKey? = runCatching { of(stringKey) }.getOrNull()

        /** Creates a key from a string with [namespace] and [name] separated by one '`:`' character. */
        public fun of(stringKey: String): PrefabKey {
            if (stringKey.split(':').size != 2)
                error("Malformed prefab key: $stringKey. Must only contain one : that splits namespace and key.")
            return PrefabKey(stringKey)
        }

        /** Creates a key from a [namespace] and [name] which must not contain any '`:`' characters. */
        public fun of(namespace: String, name: String): PrefabKey = of("$namespace:$name")
    }
}
