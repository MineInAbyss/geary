package com.mineinabyss.geary.prefabs

import com.mineinabyss.geary.datatypes.Entity

/**
 * Manages registered prefabs and accessing them via name.
 */
class PrefabManager {
    /** A list of registered [PrefabKey]s. */
    val keys: List<PrefabKey> get() = keyToPrefab.keys.toList()

    private val keyToPrefab: MutableMap<PrefabKey, Entity> = mutableMapOf()

    /** Gets a prefab by [name]. */
    operator fun get(name: PrefabKey): Entity? = keyToPrefab[name]

    /** Registers a prefab with Geary. */
    fun registerPrefab(key: PrefabKey, prefab: Entity) {
        keyToPrefab[key] = prefab
    }

    /** Gets all prefabs registered under a certain [namespace]. */
    fun getPrefabsFor(namespace: String): List<PrefabKey> =
        keys.filter { it.namespace == namespace }

    /** Clears all stored [keyToPrefab] */
    internal fun clear() {
        keyToPrefab.clear()
    }
}
