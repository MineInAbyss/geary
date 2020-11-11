package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import kotlinx.serialization.Serializable

//TODO document and maybe split into two files
@Serializable
class Children(
        internal val ids: MutableSet<GearyEntity> = mutableSetOf()
) : GearyComponent()

val GearyEntity.children
    get(): Set<GearyEntity> = get<Children>()?.ids?.toSet() ?: emptySet()

fun GearyEntity.addChild(child: GearyEntity) {
    getOrAdd { Children() }.apply {
        ids += child
    }
    child.unsafeParent = this
}

fun GearyEntity.addChildren(vararg children: GearyEntity) {
    getOrAdd { Children() }.apply {
        ids.addAll(children)
    }
    children.forEach { it.unsafeParent = this }
}

//avoid unnecessarily mutating child's parent twice when changing child's parent
private fun GearyEntity.unsafeRemoveChild(child: GearyEntity) {
    get<Children>()?.ids?.remove(child)
}

fun GearyEntity.removeChild(child: GearyEntity) {
    unsafeRemoveChild(child)
    child.unsafeParent = null
}

fun GearyEntity.removeChildren(vararg children: GearyEntity) {
    children.forEach { it.unsafeParent = null }
    get<Children>()?.ids?.removeAll(children)
}

fun GearyEntity.clearChildren() {
    val ids = get<Children>()?.ids ?: return
    ids.forEach { it.unsafeParent = null }
    ids.clear()
}


@Serializable
data class Parent(
        var id: GearyEntity?
) : GearyComponent()

/** Update child's parent without recursion. */
private var GearyEntity.unsafeParent
    get() = get<Parent>()?.id
    set(parent) {
        getOrAdd { Parent(parent) }.apply {
            this.id = parent
        }
    }

/** Set an entity's parent. Also adds/removes this child from the parent. */
var GearyEntity.parent
    get() = get<Parent>()?.id
    set(parent) {
        this.parent?.unsafeRemoveChild(this)
        parent?.addChild(this)
    }
