package com.mineinabyss.geary.ecs.entities

//TODO add documentation and maybe split into two files

import com.mineinabyss.geary.ecs.api.engine.componentId
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.relations.NoInherit
import com.mineinabyss.geary.ecs.api.relations.RelationValueId
import com.mineinabyss.geary.ecs.api.systems.QueryManager
import com.mineinabyss.geary.ecs.api.systems.family
import com.mineinabyss.geary.ecs.components.CopyToInstances
import com.mineinabyss.geary.ecs.engine.*
import com.mineinabyss.geary.ecs.prefab.PrefabKey

/** Adds a [parent] entity to this entity.  */
public fun GearyEntity.addParent(parent: GearyEntity) {
    add(parent.id.withRole(CHILDOF))
}

/** Adds a list of [parents] entities to this entity. */
public fun GearyEntity.addParents(parents: Array<GearyEntity>) {
    parents.forEach { addParent(it) }
}

/** Removes a [parent], also unlinking this child from that parent. */
public fun GearyEntity.removeParent(parent: GearyEntity) {
    remove(parent.id.withRole(CHILDOF))
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

/** Gets the first parent of this entity */
public val GearyEntity.parent: GearyEntity?
    get() {
        type.forEach { if (it.isChild()) return (it and ENTITY_MASK).toGeary() }
        return null
    }

/** Runs code on the first parent of this entity. */
public inline fun GearyEntity.onParent(parent: GearyEntity? = this.parent, run: GearyEntity.() -> Unit) {
    parent ?: return
    run(parent)
}

public val GearyEntity.parents: Set<GearyEntity>
    get() {
        val parents = mutableSetOf<GearyEntity>()
        type.forEach {
            if (id.isChild()) parents.add((id and ENTITY_MASK).toGeary())
        }
        return parents
    }

public val GearyEntity.children: List<GearyEntity>
    get() = QueryManager.getEntitiesMatching(family {
        has(id.withRole(CHILDOF))
    })

public val GearyEntity.instances: List<GearyEntity>
    get() = QueryManager.getEntitiesMatching(family {
        has(id.withRole(INSTANCEOF))
    })

public val GearyEntity.prefabKeys: List<PrefabKey>
    get() = prefabs.mapNotNull { it.get<PrefabKey>() }

public val GearyEntity.prefabs: List<GearyEntity>
    get() = type.filter { it.isInstance() }.inner.map { it.toGeary() }

/** Adds a [prefab] entity to this entity.  */
public fun GearyEntity.addPrefab(prefab: GearyEntity) {
    add(prefab.id.withRole(INSTANCEOF))
    //TODO this isn't copying over any relations
    val comp = prefab.getComponents()
    val noInherit = prefab.getComponentsRelatedTo(RelationValueId(componentId<NoInherit>()))
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
