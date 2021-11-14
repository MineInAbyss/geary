package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.systems.QueryManager

public fun GearyType.getArchetype(): Archetype {
    var node = root
    forEach { compId ->
        node = node.add[compId] ?: createArchetype(node, compId)
    }
    return node
}

private fun createArchetype(prevNode: Archetype, componentEdge: GearyComponentId): Archetype {
    val arc = Archetype(prevNode.type.plus(componentEdge))
    arc.remove[componentEdge] = prevNode
    prevNode.add[componentEdge] = arc
    QueryManager.registerArchetype(arc)
    return arc
}

public fun Archetype.countChildren(vis: MutableSet<Archetype> = mutableSetOf()): Int {
    add.values.filter { it !in vis }.forEach { it.countChildren(vis) }
    vis.addAll(add.values)
    return vis.count()
}

internal val root: Archetype = Archetype(GearyType())

public fun GearyType.plus(id: GearyComponentId): GearyType =
    GearyType(this).apply { add(id) }

public fun GearyType.minus(id: GearyComponentId): GearyType =
    GearyType(this).apply { remove(id) }
