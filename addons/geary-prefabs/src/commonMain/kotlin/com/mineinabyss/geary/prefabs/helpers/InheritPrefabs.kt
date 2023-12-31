package com.mineinabyss.geary.prefabs.helpers

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.InheritPrefabs

/**
 * Adds prefabs to this entity from an [InheritPrefabs] component. Will make sure parents have their prefabs
 * added from this component before trying to add it
 */
fun Entity.inheritPrefabs(instances: Set<Entity> = setOf()) {
    if (this in instances)
        error("Circular dependency found while loading prefabs for ${get<PrefabKey>()}, chain was: $instances")
    val add = get<InheritPrefabs>() ?: return
    remove<InheritPrefabs>()
    add.from.mapNotNull { key ->
        key.toEntityOrNull().also {
            if (it == null) geary.logger.w("Prefab ${get<PrefabKey>()} could not inherit prefab $key, it does not exist")
        }
    }.forEach { parent ->
        parent.inheritPrefabs(instances + this)
        extend(parent)
    }
}
