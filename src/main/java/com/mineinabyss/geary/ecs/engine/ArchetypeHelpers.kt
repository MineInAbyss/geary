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
    val arc = Archetype(prevNode.type.plusSorted(componentEdge))
    arc.remove[componentEdge] = prevNode
    prevNode.add[componentEdge] = arc
    SystemManager.assignArchetypeToSystems(arc)
    return arc
}

internal val root: Archetype = Archetype(listOf())

//TODO this should be using binary search to find where to add the component. Find a sorted immutable list.
public fun GearyType.plusSorted(id: GearyComponentId): GearyType = (this + id).sorted()
