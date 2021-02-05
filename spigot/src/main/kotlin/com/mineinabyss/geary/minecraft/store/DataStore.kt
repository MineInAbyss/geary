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
import org.bukkit.plugin.Plugin

internal const val COMPONENT_PREFIX = "component."

public fun <T : GearyComponent> PersistentDataContainer.encode(
    value: T,
    serializer: SerializationStrategy<T> = cborFormat.serializersModule.getPolymorphic(GearyComponent::class, value)
        ?: error("Serializer not registered for ${value::class.simpleName}"),
    key: NamespacedKey = Formats.getSerialNameFor(value::class)?.toMCKey()
        ?: error("SerialName  not registered for ${value::class.simpleName}"),
) {
    @Suppress("DEPRECATION")
    val componentKey = NamespacedKey(key.namespace, "$COMPONENT_PREFIX${key.key}")
    val encoded = cborFormat.encodeToByteArray(serializer, value)
    this[componentKey, BYTE_ARRAY] = encoded
}

public inline fun <reified T : GearyComponent> PersistentDataContainer.decode(): T? {
    return decode(
        serializer = Formats.getSerializerFor<T>() ?: return null,
        key = Formats.getSerialNameFor<T>()?.toMCKey() ?: return null
    )
}

//TODO make others pass plugin here
public inline fun <reified T : GearyComponent> PersistentDataContainer.decode(
    key: NamespacedKey,
    serializer: DeserializationStrategy<T>? = Formats.getSerializerFor(key.key) as? DeserializationStrategy<T>,
): T? {
    serializer ?: return null
    val encoded = this[key, BYTE_ARRAY] ?: return null
    return cborFormat.decodeFromByteArray(serializer, encoded)
}

public fun PersistentDataContainer.encodeComponents(components: Collection<GearyComponent>) {
    isGearyEntity = true
    //remove all keys present on the PDC so we only end up with the new list of components being encoded
    keys.filter { it.namespace == "geary" && it != SpigotEngine.componentsKey }.forEach { remove(it) }

    //get the serializer registered under the MobzyComponent class through polymorphic serialization, and use it to
    // write a serialized value under its serialname
    for (value in components)
        encode(value)
}

public fun PersistentDataContainer.decodeComponents(): Set<GearyComponent> =
    // only include keys that start with the component prefix and remove it to get the serial name
    keys.filter { it.key.startsWith(COMPONENT_PREFIX) }
        .map {
            @Suppress("DEPRECATION")
            NamespacedKey(it.namespace, it.key.removePrefix(COMPONENT_PREFIX))
        }
        .mapNotNull { decode(it) }
        .toSet()

internal fun PersistentDataContainer.keysFrom(plugin: Plugin): List<NamespacedKey> {
    val pluginNamespace = NamespacedKey(plugin, "").namespace
    return keys.filter { it.namespace == pluginNamespace }
}

public fun String.toMCKey(): NamespacedKey {
    val split = split(':')
    if (split.size != 2)
        error("Malformatted key, must only contain one : that splits namespace and key.")

    val (namespace, key) = split

    @Suppress("DEPRECATION") // deprecated just to discourage using instantiating without plugin reference
    return NamespacedKey(namespace, key)
}

public fun NamespacedKey.toSerialName(): String =
    this.key.replace("_", ":")
