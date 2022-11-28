package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.maps.TypeMap
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

public class SimpleArchetypeProvider(
    private val eventRunner: ArchetypeEventRunner,
    private val queryManager: ArchetypeQueryManager,
    private val typeMap: TypeMap
) : ArchetypeProvider {
    override val rootArchetype: Archetype = Archetype(this, typeMap, eventRunner, EntityType(), 0)
    private val archetypes = mutableListOf(rootArchetype)
    override val count: Int get() = archetypes.size

    public val archetypeCount: Int get() = archetypes.size
    private val archetypeWriteLock = SynchronizedObject()

    init {
        queryManager.registerArchetype(rootArchetype)
    }

    private fun createArchetype(prevNode: Archetype, componentEdge: ComponentId): Archetype {
        val arc = Archetype(this, typeMap, eventRunner, prevNode.type.plus(componentEdge), archetypes.size)
            .also { archetypes += it }
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
