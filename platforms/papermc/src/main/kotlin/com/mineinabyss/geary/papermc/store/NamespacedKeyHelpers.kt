package com.mineinabyss.geary.papermc.store

import com.mineinabyss.idofront.util.toMCKey
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.plugin.Plugin

/** The prefix present for keys of component [NamespacedKey]s. (i.e. `namespace:COMPONENT_PREFIX.key`) */
internal const val COMPONENT_PREFIX = "component."

/** Gets all the keys under this [PersistentDataContainer] whose namespace matches the [plugin]'s. */
internal fun PersistentDataContainer.keysFrom(plugin: Plugin): List<NamespacedKey> {
    val pluginNamespace = NamespacedKey(plugin, "").namespace
    return keys.filter { it.namespace == pluginNamespace }
}

/** Converts this string to a [NamespacedKey] with the [COMPONENT_PREFIX] on its key. */
fun String.toComponentKey(): NamespacedKey {
    val namespacedKey = toMCKey()
    return if(namespacedKey.key.startsWith(COMPONENT_PREFIX)) namespacedKey
    else "${namespacedKey.namespace}:${COMPONENT_PREFIX}${namespacedKey.key}".toMCKey()
}

/** Gets the serialName associated with this component [NamespacedKey]. */
fun NamespacedKey.toSerialName(): String = "$namespace:${key.removePrefix(COMPONENT_PREFIX)}"
