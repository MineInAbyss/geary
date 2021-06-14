package com.mineinabyss.geary.ecs.engine

import com.mineinabyss.geary.ecs.api.GearyComponentId
import com.mineinabyss.geary.ecs.api.GearyType
import com.mineinabyss.geary.ecs.api.systems.SystemManager

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
    SystemManager.registerArchetype(arc)
    return arc
}

internal val root: Archetype = Archetype(GearyType())

public fun GearyType.plus(id: GearyComponentId): GearyType =
    GearyType(this).apply { add(id) }

public fun GearyType.minus(id: GearyComponentId): GearyType =
    GearyType(this).apply { remove(id) }
