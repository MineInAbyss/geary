package com.mineinabyss.geary.prefabs.helpers

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.InheritPrefabs
import com.mineinabyss.geary.prefabs.entityOfOrNull

/**
 * Adds prefabs to this entity from an [InheritPrefabs] component. Will make sure parents have their prefabs
 * added from this component before trying to add it
 */
fun Entity.inheritPrefabsIfNeeded(instances: Set<Entity> = setOf()): Unit = with(world) {
    if (this@inheritPrefabsIfNeeded in instances)
        error("Circular dependency found while loading prefabs for ${get<PrefabKey>()}, chain was: $instances")
    val add = get<InheritPrefabs>() ?: return
    remove<InheritPrefabs>()
    add.from.mapNotNull { key ->
        entityOfOrNull(key).also {
            if (it == null) logger.w("Prefab ${get<PrefabKey>()} could not inherit prefab $key, it does not exist")
        }
    }.forEach { parent ->
        parent.inheritPrefabsIfNeeded(instances + this@inheritPrefabsIfNeeded)
        extend(parent)
    }
}
