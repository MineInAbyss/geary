package com.mineinabyss.geary.prefabs.helpers

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances

val Entity.prefabs: List<Entity>
    get() = getRelations<InstanceOf?, Any?>().map { it.target.toGeary() }

/** Adds a [prefab] entity to this entity.  */
fun Entity.addPrefab(prefab: Entity) {
    addRelation<InstanceOf>(prefab)
    //TODO this isn't copying over any relations
    val comp = prefab.getAll().toMutableSet()
    prefab.getRelationsWithData<NoInherit?, Any>().forEach {
        comp -= it.targetData
    }
    prefab.children.forEach { it.addParent(this) }
    setAll(comp, override = false) //TODO plan out more thoroughly and document overriding behaviour
    prefab.with { copy: CopyToInstances ->
        copy.decodeComponentsTo(this, override = false)
    }
}

/** Adds a [prefab] entity to this entity.  */
fun Entity.removePrefab(prefab: Entity) {
    removeRelation<InstanceOf>(prefab)
}
