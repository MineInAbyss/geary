package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.modules.archetypes
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class SimpleArchetypeProvider : ArchetypeProvider {
    private val queryManager: ArchetypeQueryManager get() = archetypes.queryManager

    override val rootArchetype: Archetype by lazy {
        Archetype(EntityType(), 0).also {
            queryManager.registerArchetype(it)
        }
    }
    override val count: Int get() = trackedArchetypes.size

    private val trackedArchetypes by lazy { mutableListOf(rootArchetype) }
    private val archetypeWriteLock = SynchronizedObject()


    private fun createArchetype(prevNode: Archetype, componentEdge: ComponentId): Archetype {
        val arc = Archetype(prevNode.type.plus(componentEdge), trackedArchetypes.size)
            .also { trackedArchetypes += it }
        arc.componentRemoveEdges[componentEdge] = prevNode
        prevNode.componentAddEdges[componentEdge] = arc
        queryManager.registerArchetype(arc)
        return arc
    }

    override fun getArchetype(entityType: EntityType): Archetype = synchronized(archetypeWriteLock) {
        var node = rootArchetype
        entityType.forEach { compId ->
            node = node.componentAddEdges[compId] ?: createArchetype(node, compId)
        }
        return node
    }
}
