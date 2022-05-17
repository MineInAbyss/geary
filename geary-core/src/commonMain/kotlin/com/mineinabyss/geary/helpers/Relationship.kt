package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.datatypes.GearyEntity

/** Adds a [parent] entity to this entity.  */
public fun GearyEntity.addParent(parent: GearyEntity) {
    addRelation<ChildOf>(parent)
}

/** Adds a list of [parents] entities to this entity. */
public fun GearyEntity.addParents(parents: Array<GearyEntity>) {
    parents.forEach { addParent(it) }
}

/** Removes a [parent], also unlinking this child from that parent. */
public fun GearyEntity.removeParent(parent: GearyEntity) {
    removeRelation<ChildOf>(parent)
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
    get() = getRelations<ChildOf?, Any?>().firstOrNull()?.targetEntity

/** Runs code on the first parent of this entity. */
public inline fun GearyEntity.onParent(
    parent: GearyEntity? = this.parent,
    run: GearyEntity.() -> Unit
) {
    parent ?: return
    run(parent)
}

public val GearyEntity.parents: Set<GearyEntity>
    get() = getRelations<ChildOf?, Any?>().mapTo(mutableSetOf()) { it.targetEntity }
