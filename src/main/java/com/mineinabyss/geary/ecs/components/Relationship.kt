package com.mineinabyss.geary.ecs.components

import com.mineinabyss.geary.ecs.GearyComponent
import com.mineinabyss.geary.ecs.GearyEntity
import kotlinx.serialization.Serializable

//TODO document and maybe split into two files
@Serializable
public class Children(
        internal val ids: MutableSet<GearyEntity> = mutableSetOf()
) : GearyComponent()

public val GearyEntity.children: Set<GearyEntity>
    get() = get<Children>()?.ids?.toSet() ?: emptySet()

public fun GearyEntity.addChild(child: GearyEntity) {
    getOrAdd { Children() }.apply {
        ids += child
    }
    child.unsafeParent = this
}

public fun GearyEntity.addChildren(vararg children: GearyEntity) {
    getOrAdd { Children() }.apply {
        ids.addAll(children)
    }
    children.forEach { it.unsafeParent = this }
}

//avoid unnecessarily mutating child's parent twice when changing child's parent
private fun GearyEntity.unsafeRemoveChild(child: GearyEntity) {
    get<Children>()?.ids?.remove(child)
}

public fun GearyEntity.removeChild(child: GearyEntity) {
    unsafeRemoveChild(child)
    child.unsafeParent = null
}

public fun GearyEntity.removeChildren(vararg children: GearyEntity) {
    children.forEach { it.unsafeParent = null }
    get<Children>()?.ids?.removeAll(children)
}

public fun GearyEntity.clearChildren() {
    val ids = get<Children>()?.ids ?: return
    ids.forEach { it.unsafeParent = null }
    ids.clear()
}


@Serializable
public data class Parent(
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
public var GearyEntity.parent: GearyEntity?
    get() = get<Parent>()?.id
    set(parent) {
        this.parent?.unsafeRemoveChild(this)
        parent?.addChild(this)
    }
