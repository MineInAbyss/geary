package com.mineinabyss.geary.ecs.entities

//TODO add documentation and maybe split into two files

import com.mineinabyss.geary.ecs.api.engine.EngineContext
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.toGeary
import com.mineinabyss.geary.ecs.api.systems.QueryContext
import com.mineinabyss.geary.ecs.engine.CHILDOF
import com.mineinabyss.geary.ecs.engine.ENTITY_MASK
import com.mineinabyss.geary.ecs.engine.isChild
import com.mineinabyss.geary.ecs.engine.withRole

/** Adds a [parent] entity to this entity.  */
context(EngineContext) public fun GearyEntity.addParent(parent: GearyEntity) {
    add(parent.id.withRole(CHILDOF))
}

/** Adds a list of [parents] entities to this entity. */
context(EngineContext) public fun GearyEntity.addParents(parents: Array<GearyEntity>) {
    parents.forEach { addParent(it) }
}

/** Removes a [parent], also unlinking this child from that parent. */
context(EngineContext) public fun GearyEntity.removeParent(parent: GearyEntity) {
    remove(parent.id.withRole(CHILDOF))
}

/** Removes all of this entity's parents, also unlinking this child from them. */
context(EngineContext) public fun GearyEntity.clearParents() {
    parents.forEach { remove(it.id) }
}

/** Adds a [child] entity to this entity.  */
context(EngineContext) public fun GearyEntity.addChild(child: GearyEntity) {
    child.addParent(this)
}

/** Adds a list of [children] entities to this entity. */
context(EngineContext) public fun GearyEntity.addChildren(children: Array<GearyEntity>) {
    children.forEach { addChild(it) }
}

/** Removes a [child], also unlinking this parent from that child. */
context(EngineContext) public fun GearyEntity.removeChild(child: GearyEntity) {
    child.removeParent(this)
}

/** Removes all of this entity's children, also unlinking this parent from them. */
context(EngineContext, QueryContext) public fun GearyEntity.clearChildren() {
    children.forEach { remove(it.id) }
}

/** Gets the first parent of this entity */
context(EngineContext) public val GearyEntity.parent: GearyEntity?
    get() {
        type.forEach { if (it.isChild()) return (it and ENTITY_MASK).toGeary() }
        return null
    }

/** Runs code on the first parent of this entity. */
context(EngineContext) public inline fun GearyEntity.onParent(
    parent: GearyEntity? = this.parent,
    run: GearyEntity.() -> Unit
) {
    parent ?: return
    run(parent)
}

context(EngineContext) public val GearyEntity.parents: Set<GearyEntity>
    get() {
        val parents = mutableSetOf<GearyEntity>()
        type.forEach {
            if (id.isChild()) parents.add((id and ENTITY_MASK).toGeary())
        }
        return parents
    }
