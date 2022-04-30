package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.context.globalContext
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.prefabs.serializers.PrefabKeySerializer
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

/**
 * An inline class which represents a key build from a [namespace] and [key], separated
 * by a '`:`' symbol.
 */
@Serializable(with = PrefabKeySerializer::class)
@JvmInline
public value class PrefabKey private constructor(public val full: String) : KoinComponent {
    public val namespace: String get() = full.substringBefore(':')
    public val key: String get() = full.substringAfter(':')

    public fun toEntity(): GearyEntity? = globalContext.prefabManager[this]

    override fun toString(): String = full

    public companion object {
        /** Creates a key like [of] but returns null when parsing fails. */
        public fun ofOrNull(stringKey: String): PrefabKey? = runCatching { of(stringKey) }.getOrNull()

        /** Creates a key from a string with [namespace] and [key] separated by one '`:`' character. */
        public fun of(stringKey: String): PrefabKey {
            if (stringKey.split(':').size != 2)
                error("Malformed prefab key: $stringKey. Must only contain one : that splits namespace and key.")
            return PrefabKey(stringKey)
        }

        /** Creates a key from a [namespace] and [name] which must not contain any '`:`' characters. */
        public fun of(namespace: String, name: String): PrefabKey = of("$namespace:$name")
    }
}
