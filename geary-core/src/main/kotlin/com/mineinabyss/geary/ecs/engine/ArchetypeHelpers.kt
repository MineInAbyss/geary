package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.engine.Engine

public fun GearyType.getArchetype(): Archetype = Engine.getArchetype(this)

public fun Archetype.countChildren(vis: MutableSet<Archetype> = mutableSetOf()): Int {
    componentAddEdges.values.filter { it !in vis }.forEach { it.countChildren(vis) }
    vis.addAll(componentAddEdges.values)
    return vis.count()
}
