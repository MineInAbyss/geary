package com.mineinabyss.geary.ecs.entities

//TODO add documentation and maybe split into two files

import com.mineinabyss.geary.ecs.api.engine.type
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.api.systems.Family
import com.mineinabyss.geary.ecs.api.systems.SystemManager
import com.mineinabyss.geary.ecs.components.CopyToInstances
import com.mineinabyss.geary.ecs.components.PrefabKey
import com.mineinabyss.geary.ecs.engine.CHILDOF
import com.mineinabyss.geary.ecs.engine.ENTITY_MASK
import com.mineinabyss.geary.ecs.engine.INSTANCEOF

/** Adds a [parent] entity to this entity.  */
public fun GearyEntity.addParent(parent: GearyEntity) {
    add(parent.id or CHILDOF)
}

/** Adds a list of [parents] entities to this entity. */
public fun GearyEntity.addParents(parents: Array<GearyEntity>) {
    parents.forEach { addParent(it) }
}

/** Removes a [parent], also unlinking this child from that parent. */
public fun GearyEntity.removeParent(parent: GearyEntity) {
    remove(parent.id or CHILDOF)
}

/** Removes all of this entity's parents, also unlinking this child from them. */
public fun GearyEntity.clearParents() {
    parents.forEach { remove(it.id) }
}

/** Adds a [child] entity to this entity.  */
public fun GearyEntity.addChild(child: GearyEntity) {
    child.addParent(this)
}

/** Adds a list of [children] entities to this entity. */
public fun GearyEntity.addChildren(children: Array<GearyEntity>) {
    children.forEach { addChild(it) }
}

/** Removes a [child], also unlinking this parent from that child. */
public fun GearyEntity.removeChild(child: GearyEntity) {
    child.removeParent(this)
}

/** Removes all of this entity's children, also unlinking this parent from them. */
public fun GearyEntity.clearChildren() {
    children.forEach { remove(it.id) }
}

public val GearyEntity.parent: GearyEntity?
    get() = type.firstOrNull { id and CHILDOF != 0uL }?.let { geary(it) }

public val GearyEntity.parents: Set<GearyEntity>
    get() {
        val parents = mutableSetOf<GearyEntity>()
        for (id in type) if (id and CHILDOF != 0uL)
            parents.add(geary(id and ENTITY_MASK))
        return parents
    }

public val GearyEntity.children: List<GearyEntity>
    get() = SystemManager.getEntitiesMatching(Family(sortedSetOf(CHILDOF or id)))

public val GearyEntity.prefabs: List<PrefabKey>
    get() = type.filter { it and INSTANCEOF != 0uL }.mapNotNull { geary(it).get<PrefabKey>() }


/** Adds a [prefab] entity to this entity.  */
public fun GearyEntity.addPrefab(prefab: GearyEntity) {
    add(prefab.id or INSTANCEOF)
    setAll(prefab.getComponents())
    prefab.with<CopyToInstances> {
        it.decodeComponentsTo(this)
    }
}

/** Adds a [prefab] entity to this entity.  */
public fun GearyEntity.removePrefab(prefab: GearyEntity) {
    remove(prefab.id or INSTANCEOF)
}
