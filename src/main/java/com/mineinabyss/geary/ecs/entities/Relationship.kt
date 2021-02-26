package com.mineinabyss.geary.ecs.entities

//TODO add documentation and maybe split into two files

import com.mineinabyss.geary.ecs.api.engine.Engine
import com.mineinabyss.geary.ecs.api.entities.GearyEntity
import com.mineinabyss.geary.ecs.api.entities.geary
import com.mineinabyss.geary.ecs.engine.GearyEngine
import com.mineinabyss.geary.ecs.engine.types.CHILDOF
import com.mineinabyss.geary.ecs.engine.types.ENTITY_MASK
import com.mineinabyss.geary.ecs.engine.types.type

/** Adds a [Children] component, adding the [child] to it. Also sets the parent of [child] to this entity.  */
public fun GearyEntity.addChild(child: GearyEntity) {
    set(child.id or CHILDOF)
}

/** Adds a [Children] component, adding the [children] to it. Also sets the parents of [children] to this entity. */
public fun GearyEntity.addChildren(children: Array<GearyEntity>) {
    children.forEach { addChild(it) }
}

/** Removes a [child], also removing its parent. */
public fun GearyEntity.removeChild(child: GearyEntity) {
    unset(child.id or CHILDOF)
}

/** Removes all of this entity's children, also removing their parents. */
public fun GearyEntity.clearParents(): Boolean =
    type.removeAll(parents.map { it.id })

public val GearyEntity.parent: GearyEntity?
    get() = type.firstOrNull { id and CHILDOF != 0uL }?.let { geary(it) }

public val GearyEntity.parents: Set<GearyEntity>
    get() {
        val parents = mutableSetOf<GearyEntity>()
        for (id in type) if (id and CHILDOF != 0uL)
            parents.add(geary(id and ENTITY_MASK))
        return parents
    }

public val GearyEntity.children: Set<GearyEntity>
    get() =
        //TODO use family access here
        (Engine as GearyEngine).getComponentArrayFor(CHILDOF or id)
            .unpackedIndices
            .map { geary(it.toULong()) }
            .toSet()
