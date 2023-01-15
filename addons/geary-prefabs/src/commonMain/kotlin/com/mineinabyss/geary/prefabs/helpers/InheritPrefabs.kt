package com.mineinabyss.geary.prefabs.helpers

import com.mineinabyss.geary.datatypes.Entity
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
    add.from.mapNotNull { it.toEntityOrNull() }
        .forEach { parent ->
            parent.inheritPrefabs(instances + this)
            addPrefab(parent)
        }
}
