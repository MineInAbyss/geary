package com.mineinabyss.geary.ecs.components

//TODO add documentation and maybe split into two files

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity

/**
 * A component holding the children entities of this entity.
 */
//TODO have UUIDs for referencing children/parents after an engine restart, then make serializable
public class Children(
        internal val ids: MutableSet<GearyEntity> = mutableSetOf()
) : GearyComponent

/** A list of children for this entity */
public val GearyEntity.children: Set<GearyEntity>
    get() = get<Children>()?.ids?.toSet() ?: emptySet()

/** Adds a [Children] component, adding the [child] to it. Also sets the parent of [child] to this entity.  */
public fun GearyEntity.addChild(child: GearyEntity) {
    getOrAdd { Children() }.apply {
        ids += child
    }
    child.unsafeParent = this
}

/** Adds a [Children] component, adding the [children] to it. Also sets the parents of [children] to this entity. */
public fun GearyEntity.addChildren(vararg children: GearyEntity) {
    getOrAdd { Children() }.apply {
        ids.addAll(children)
    }
    children.forEach { it.unsafeParent = this }
}

/** Removes a [child] without also removing its parent. */
private fun GearyEntity.unsafeRemoveChild(child: GearyEntity) {
    get<Children>()?.ids?.remove(child)
}

/** Removes a [child], also removing its parent. */
public fun GearyEntity.removeChild(child: GearyEntity) {
    unsafeRemoveChild(child)
    child.unsafeParent = null
}

/** Removes [children], also removing their parents. */
public fun GearyEntity.removeChildren(vararg children: GearyEntity) {
    children.forEach { it.unsafeParent = null }
    get<Children>()?.ids?.removeAll(children)
}

/** Removes all of this entity's children, also removing their parents. */
public fun GearyEntity.clearChildren() {
    val ids = get<Children>()?.ids ?: return
    ids.forEach { it.unsafeParent = null }
    ids.clear()
}

/**
 * A component holding the parent entity of this entity.
 */
public data class Parent(
        var id: GearyEntity?
) : GearyComponent

/** Update child's parent without also adding it to the parent's children. */
private var GearyEntity.unsafeParent
    get() = get<Parent>()?.id
    set(parent) {
        getOrAdd { Parent(parent) }.apply {
            this.id = parent
        }
    }

/** Set an entity's parent. Also adds/removes this child from the parent. */
public var GearyEntity.parent: GearyEntity?
    get() = get<Parent>()?.id
    set(parent) {
        // removes this entity from the old parent's children
        this.parent?.unsafeRemoveChild(this)
        // adds this entity to the new parent's children, which also adds a parent component to this entity
        parent?.addChild(this)
    }
