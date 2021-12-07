package com.mineinabyss.geary.ecs.prefab

import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.components.InheritPrefabs
import com.mineinabyss.geary.ecs.entities.addPrefab

/**
 * Adds prefabs to this entity from an [InheritPrefabs] component. Will make sure parents have their prefabs
 * added from this component before trying to add it
 */
public fun GearyEntity.inheritPrefabs(instances: Set<GearyEntity> = setOf()) {
    if (this in instances)
        error("Circular dependency found while loading prefabs for ${get<PrefabKey>()}, chain was: $instances")
    val add = get<InheritPrefabs>() ?: return
    remove<InheritPrefabs>()
    add.from.mapNotNull { it.toEntity() }
        .forEach { parent ->
            parent.inheritPrefabs(instances + this)
            addPrefab(parent)
        }
}
