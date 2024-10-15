package com.mineinabyss.geary.helpers

import com.mineinabyss.geary.components.CouldHaveChildren
import com.mineinabyss.geary.components.relations.ChildOf
import com.mineinabyss.geary.datatypes.Entity

/** Adds a [parent] entity to this entity.  */
fun Entity.addParent(parent: Entity) {
    parent.add<CouldHaveChildren>() // TODO temporarily in place until child queries are faster
    addRelation<ChildOf>(parent)
}

/** Adds a list of [parents] entities to this entity. */
fun Entity.addParents(parents: Array<Entity>) {
    parents.fastForEach { addParent(it) }
}

/** Removes a [parent], also unlinking this child from that parent. */
fun Entity.removeParent(parent: Entity) {
    removeRelation<ChildOf>(parent)
}

/** Removes all of this entity's parents, also unlinking this child from them. */
fun Entity.clearParents() {
    parents.forEach { remove(it.id) }
}

/** Adds a [child] entity to this entity.  */
fun Entity.addChild(child: Entity) {
    child.addParent(this)
}

/** Adds a list of [children] entities to this entity. */
fun Entity.addChildren(children: Array<Entity>) {
    children.fastForEach { addChild(it) }
}

/** Removes a [child], also unlinking this parent from that child. */
fun Entity.removeChild(child: Entity) {
    child.removeParent(this)
}

/** Removes all of this entity's children, also unlinking this parent from them. */
fun Entity.clearChildren() {
    children.fastForEach { remove(it.id) }
}

/** Gets the first parent of this entity */
val Entity.parent: Entity?
    get() = getRelations<ChildOf?, Any?>().firstOrNull()?.target?.toGeary(world)

/** Runs code on the first parent of this entity. */
inline fun Entity.onParent(
    parent: Entity? = this.parent,
    run: Entity.() -> Unit
) {
    parent ?: return
    run(parent)
}

val Entity.parents: Set<Entity>
    get() = getRelations<ChildOf?, Any?>().mapTo(mutableSetOf()) { it.target.toGeary(world) }
