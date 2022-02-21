package com.mineinabyss.geary.prefabs.helpers

import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.relations.NoInherit
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.engine.INSTANCEOF
import com.mineinabyss.geary.ecs.engine.isInstance
import com.mineinabyss.geary.ecs.engine.withRole
import com.mineinabyss.geary.ecs.entities.addParent
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances

//public val GearyEntity.prefabKeys: List<PrefabKey>
//    get() = prefabs.mapNotNull { it.get<PrefabKey>() }

public val GearyEntity.prefabs: List<GearyEntity>
    get() = type.filter { it.isInstance() }.map { it.toGeary() }

/** Adds a [prefab] entity to this entity.  */
public fun GearyEntity.addPrefab(prefab: GearyEntity) {
    add(prefab.id.withRole(INSTANCEOF))
    //TODO this isn't copying over any relations
    val comp = prefab.getComponents()
    val noInherit = prefab.getRelationsByValue(RelationValueId(componentId<NoInherit>()))
    prefab.children.forEach { it.addParent(this) }
    setAll((comp - noInherit), override = false) //TODO plan out more thoroughly and document overriding behaviour
    prefab.with { copy: CopyToInstances ->
        copy.decodeComponentsTo(this, override = false)
    }
}

/** Adds a [prefab] entity to this entity.  */
public fun GearyEntity.removePrefab(prefab: GearyEntity) {
    remove(prefab.id.withRole(INSTANCEOF))
}
