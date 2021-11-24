package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType

public fun GearyType.getArchetype(engine: GearyEngine): Archetype {
    var node = engine.root
    forEach { compId ->
        node = node.componentAddEdges[compId] ?: createArchetype(node, compId)
    }
    return node
}

private fun createArchetype(prevNode: Archetype, componentEdge: GearyComponentId): Archetype {
    val arc = Archetype(prevNode.type.plus(componentEdge), prevNode.engine)
    arc.componentRemoveEdges[componentEdge] = prevNode
    prevNode.componentAddEdges[componentEdge] = arc
    arc.engine.queryManager.registerArchetype(arc)
    return arc
}

public fun Archetype.countChildren(vis: MutableSet<Archetype> = mutableSetOf()): Int {
    componentAddEdges.values.filter { it !in vis }.forEach { it.countChildren(vis) }
    vis.addAll(componentAddEdges.values)
    return vis.count()
}

public fun GearyType.plus(id: GearyComponentId): GearyType =
    GearyType(this).apply { add(id) }

public fun GearyType.minus(id: GearyComponentId): GearyType =
    GearyType(this).apply { remove(id) }
