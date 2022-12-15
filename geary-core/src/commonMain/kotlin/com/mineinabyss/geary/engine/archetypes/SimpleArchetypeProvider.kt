package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.context.archetypes
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.maps.TypeMap
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class SimpleArchetypeProvider : ArchetypeProvider {
    private val eventRunner: ArchetypeEventRunner get() = archetypes.eventRunner
    private val queryManager: ArchetypeQueryManager get() = archetypes.queryManager
    private val records: TypeMap get() = archetypes.records

    override val rootArchetype: Archetype by lazy {
        Archetype(this, records, eventRunner, EntityType(), 0).also {
            queryManager.registerArchetype(it)
        }
    }
    private val trackedArchetypes = mutableListOf(rootArchetype)
    override val count: Int get() = trackedArchetypes.size

    val archetypeCount: Int get() = trackedArchetypes.size
    private val archetypeWriteLock = SynchronizedObject()

    private fun createArchetype(prevNode: Archetype, componentEdge: ComponentId): Archetype {
        val arc = Archetype(this, records, eventRunner, prevNode.type.plus(componentEdge), trackedArchetypes.size)
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
