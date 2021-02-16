package com.mineinabyss.geary.minecraft.store

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.serialization.Formats
import com.mineinabyss.geary.ecs.serialization.Formats.cborFormat
import com.mineinabyss.geary.minecraft.engine.SpigotEngine
import com.mineinabyss.geary.minecraft.isGearyEntity
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
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
    val encoded = this[key.addComponentPrefix(), BYTE_ARRAY] ?: return null
    return cborFormat.decodeFromByteArray(serializer, encoded)
}

/**
 * Encodes a list of [components] to this [PersistentDataContainer].
 *
 * @see encode
 */
public fun PersistentDataContainer.encodeComponents(components: Collection<GearyComponent>) {
    isGearyEntity = true
    //remove all keys present on the PDC so we only end up with the new list of components being encoded
    keys.filter { it.namespace == "geary" && it != SpigotEngine.componentsKey }.forEach { remove(it) }

    for (value in components)
        encode(value)
}

/**
 * Decodes a set of components from this [PersistentDataContainer].
 *
 * @see decode
 */
public fun PersistentDataContainer.decodeComponents(): Set<GearyComponent> =
    // only include keys that start with the component prefix and remove it to get the serial name
    keys.filter { it.key.startsWith(COMPONENT_PREFIX) }
        .map { it.removeComponentPrefix() }
        .mapNotNull { decode(it) }
        .toSet()
