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
value class PrefabKey private constructor(val full: String) : KoinComponent {
    val namespace: String get() = full.substringBefore(':')
    val key: String get() = full.substringAfter(':')

    fun toEntity(): GearyEntity = toEntityOrNull()
        ?: error("Requested non null prefab entity for $this, but it does not exist.")

    fun toEntityOrNull(): GearyEntity? = globalContext.prefabManager[this]

    override fun toString(): String = full

    companion object {
        /** Creates a key like [of] but returns null when parsing fails. */
        fun ofOrNull(stringKey: String): PrefabKey? = runCatching { of(stringKey) }.getOrNull()

        /** Creates a key from a string with [namespace] and [key] separated by one '`:`' character. */
        fun of(stringKey: String): PrefabKey {
            if (stringKey.split(':').size != 2)
                error("Malformed prefab key: $stringKey. Must only contain one : that splits namespace and key.")
            return PrefabKey(stringKey)
        }

        /** Creates a key from a [namespace] and [name] which must not contain any '`:`' characters. */
        fun of(namespace: String, name: String): PrefabKey = of("$namespace:$name")
    }
}

