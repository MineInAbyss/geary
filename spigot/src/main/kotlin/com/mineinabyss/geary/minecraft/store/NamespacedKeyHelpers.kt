package com.mineinabyss.geary.minecraft.store

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.plugin.Plugin

/** Gets all the keys under this [PersistentDataContainer] whose namespace matches the [plugin]'s. */
internal fun PersistentDataContainer.keysFrom(plugin: Plugin): List<NamespacedKey> {
    val pluginNamespace = NamespacedKey(plugin, "").namespace
    return keys.filter { it.namespace == pluginNamespace }
}

/** Converts this string to a [NamespacedKey] where the string must be formatted as `namespace:key` */
public fun String.toMCKey(): NamespacedKey {
    val split = split(':')
    if (split.size != 2)
        error("Malformatted key, must only contain one : that splits namespace and key.")

    val (namespace, key) = split

    @Suppress("DEPRECATION") // deprecated just to discourage using instantiating without plugin reference
    return NamespacedKey(namespace, key)
}

/** Converts this string to a [NamespacedKey] with the [COMPONENT_PREFIX] on its key. */
public fun String.toComponentKey(): NamespacedKey = toMCKey().addComponentPrefix()

/** Gets the serialName associated with this component [NamespacedKey]. */
public fun NamespacedKey.toSerialName(): String = "$namespace:${key.removePrefix(COMPONENT_PREFIX)}"

/** The prefix present for keys of component [NamespacedKey]s. (i.e. `namespace:COMPONENT_PREFIX.key`) */
internal const val COMPONENT_PREFIX = "component."

/** Adds the [COMPONENT_PREFIX] to this [NamespacedKey] if not already present. */
public fun NamespacedKey.addComponentPrefix(): NamespacedKey {
    if (key.startsWith(COMPONENT_PREFIX)) return this

    @Suppress("DEPRECATION")
    return NamespacedKey(namespace, "$COMPONENT_PREFIX${key}")
}
