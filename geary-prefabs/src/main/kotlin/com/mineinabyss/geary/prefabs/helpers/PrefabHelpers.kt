package com.mineinabyss.geary.prefabs.helpers

import com.mineinabyss.geary.components.relations.DontInherit
import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.addParent
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.helpers.with
import com.mineinabyss.geary.prefabs.configuration.components.CopyToInstances

val GearyEntity.prefabs: List<GearyEntity>
    get() = getRelations<InstanceOf?, Any?>().map { it.target.toGeary() }

/** Adds a [prefab] entity to this entity.  */
fun GearyEntity.addPrefab(prefab: GearyEntity) {
    addRelation<InstanceOf>(prefab)
    //TODO this isn't copying over any relations
    val comp = prefab.getAll().toMutableSet()
    prefab.getRelations<DontInherit?, Any?>().forEach {
        comp -= it.target
    }
    prefab.children.forEach { it.addParent(this) }
    setAll(comp, override = false) //TODO plan out more thoroughly and document overriding behaviour
    prefab.with { copy: CopyToInstances ->
        copy.decodeComponentsTo(this, override = false)
    }
}

/** Adds a [prefab] entity to this entity.  */
fun GearyEntity.removePrefab(prefab: GearyEntity) {
    removeRelation<InstanceOf>(prefab)
}
