package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.prefab.PrefabManager.types
import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf

/**
 * Manages registered [GearyPrefab]s and accessing them via name, bukkit entity, etc...
 *
 * @property types A map of [GearyPrefab]s registered with the plugin.
 */
public object PrefabManager {
    public val types: List<String> get() = prefabs.keys.toList()

    //TODO we should have our own impl of namespacedkeys that doesn't depend on spigot since we can't use them here
    private val prefabs: MutableBiMap<String, GearyPrefab> = mutableBiMapOf()

    /** Get a prefab by [name]. */
    public operator fun get(name: String): GearyPrefab? = prefabs[name]

    //TODO set the template name upon instantiation
    /** Gets the entity name from a type [T] if registered, otherwise throws an [IllegalArgumentException]*/
    public fun getNameForPrefab(prefab: GearyPrefab): String =
        prefabs.inverse[prefab] ?: error("A prefab was created but not registered in the prefab manager: $prefab")

    //TODO perhaps better immutability
    /** Registers a prefab with Geary. */
    public fun registerPrefab(name: String, prefab: GearyPrefab) {
        prefabs[name] = prefab
    }

    //TODO should maybe be internal
    /** Clears all stored [prefabs], but not [persistentTypes] */
    internal fun clear() {
        prefabs.clear()
    }
}
