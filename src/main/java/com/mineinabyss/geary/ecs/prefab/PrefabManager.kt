package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.GearyEntity
import com.mineinabyss.geary.ecs.components.GearyPrefab
import com.mineinabyss.geary.ecs.components.PrefabKey
import com.mineinabyss.geary.ecs.components.addComponent
import com.mineinabyss.geary.ecs.prefab.PrefabManager.keys
import com.uchuhimo.collections.MutableBiMap
import com.uchuhimo.collections.mutableBiMapOf

/**
 * Manages registered [GearyPrefab]s and accessing them via name, bukkit entity, etc...
 *
 * @property keys A map of [GearyPrefab]s registered with the plugin.
 */
public object PrefabManager {
    public val keys: List<PrefabKey> get() = prefabs.keys.toList()

    //TODO we should have our own impl of namespacedkeys that doesn't depend on spigot since we can't use them here
    private val prefabs: MutableBiMap<PrefabKey, GearyEntity> = mutableBiMapOf()

    /** Get a prefab by [name]. */
    public operator fun get(name: PrefabKey): GearyEntity? = prefabs[name]

    //TODO perhaps better immutability
    /** Registers a prefab with Geary. */
    public fun registerPrefab(name: PrefabKey, prefab: GearyEntity) {
        prefabs[name] = prefab
        prefab.addComponent(name)
    }

    /** Clears all stored [prefabs], but not [persistentTypes] */
    internal fun clear() {
        prefabs.clear()
    }
}
