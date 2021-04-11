package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.api.GearyComponent
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.INSTANCEOF
import com.mineinabyss.geary.ecs.prefab.PrefabKey
import com.mineinabyss.geary.ecs.prefab.PrefabManager
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.Formats.cborFormat
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.hasComponentsEncoded
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.BYTE_ARRAY

/** Returns whether or not this [PersistentDataContainer] has a component [T] encoded in it. */
public inline fun <reified T : GearyComponent> PersistentDataContainer.has(): Boolean {
    return has(Formats.getNamespacedKeyFor<T>() ?: return false, BYTE_ARRAY)
}

/**
 * Encodes a component into this [PersistentDataContainer], where the serializer and key can automatically be found via
 * [Formats].
 */
public fun <T : GearyComponent> PersistentDataContainer.encode(
    value: T,
    serializer: SerializationStrategy<T> = cborFormat.serializersModule.getPolymorphic(GearyComponent::class, value)
        ?: error("Serializer not registered for ${value::class.simpleName}"),
    key: NamespacedKey = Formats.getSerialNameFor(value::class)?.toComponentKey()
        ?: error("SerialName  not registered for ${value::class.simpleName}"),
) {
    hasComponentsEncoded = true
    val encoded = cborFormat.encodeToByteArray(serializer, value)
    this[key, BYTE_ARRAY] = encoded
}

/**
 * Decodes a component of type [T] from this [PersistentDataContainer], where serializer and key are automatically
 * found via [Formats].
 */
public inline fun <reified T : GearyComponent> PersistentDataContainer.decode(): T? {
    return decode(
        serializer = Formats.getSerializerFor<T>() ?: return null,
        key = Formats.getSerialNameFor<T>()?.toComponentKey() ?: return null
    )
}

/**
 * Decodes a component of type [T] from this [PersistentDataContainer] where the [serializer] may automatically be found
 * via [Formats] given a [key].
 */
public inline fun <reified T : GearyComponent> PersistentDataContainer.decode(
    key: NamespacedKey,
    serializer: DeserializationStrategy<T>? = Formats.getSerializerFor(key) as? DeserializationStrategy<T>,
): T? {
    serializer ?: return null
    val encoded = this[key, BYTE_ARRAY] ?: return null
    return cborFormat.decodeFromByteArray(serializer, encoded)
}

/**
 * Encodes a list of [components] to this [PersistentDataContainer].
 *
 * @see encode
 */
public fun PersistentDataContainer.encodeComponents(components: Collection<GearyComponent>, type: GearyType) {
    hasComponentsEncoded = true
    //remove all keys present on the PDC so we only end up with the new list of components being encoded
    keys.filter { it.namespace == "geary" && it != SpigotEngine.componentsKey }.forEach { remove(it) }

    for (value in components)
        encode(value)

    //encode all the prefabs of this type with a key stored under a special key. This could have been done via a
    // persisting prefab component, but I prefer being explicit and avoiding the possibility of this component
    // accidentally ending up on the entity itself
    encode(
        type.filter { it and INSTANCEOF != 0uL }
            .map { it and INSTANCEOF.inv() }
            .mapNotNull { geary(it).get<PrefabKey>() },
        ListSerializer(PrefabKey.serializer()),
        "geary:prefabs".toMCKey()
    )
}

/**
 * Decodes a set of components from this [PersistentDataContainer].
 *
 * @see decode
 */
public fun PersistentDataContainer.decodeComponents(): Pair<Set<GearyComponent>, GearyType> =
    // only include keys that start with the component prefix and remove it to get the serial name
    keys.filter { it.key.startsWith(COMPONENT_PREFIX) }
        .map { it.removeComponentPrefix() }
        .mapNotNull { decode(it) }
        .toSet() to
            (decode(
                "geary:prefabs".toMCKey(),
                ListSerializer(PrefabKey.serializer())
            ) ?: emptyList()).mapNotNullTo(sortedSetOf()) { PrefabManager[it]?.id }
