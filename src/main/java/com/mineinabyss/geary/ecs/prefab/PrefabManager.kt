package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.PrefabKey
import com.mineinabyss.geary.ecs.prefab.PrefabManager.keys
import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf

/**
 * Manages registered prefabs and accessing them via name.
 *
 * @property keys A list of registered [PrefabKey]s.
 */
public object PrefabManager {
    public val keys: List<PrefabKey> get() = prefabs.keys.toList()

    private val prefabs: MutableBiMap<PrefabKey, GearyEntity> = mutableBiMapOf()

    /** Gets a prefab by [name]. */
    public operator fun get(name: PrefabKey): GearyEntity? = prefabs[name]

    /** Registers a prefab with Geary. */
    public fun registerPrefab(name: PrefabKey, prefab: GearyEntity) {
        prefabs[name] = prefab
        prefab.set(name)
    }

    /** Clears all stored [prefabs] */
    internal fun clear() {
        prefabs.clear()
    }
}
