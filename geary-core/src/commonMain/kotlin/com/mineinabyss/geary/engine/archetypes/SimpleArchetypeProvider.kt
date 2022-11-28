package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EventRunner
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public class SimpleArchetypeProvider : ArchetypeProvider, KoinComponent {
    private val queryManager: ArchetypeQueryManager by inject()
    private val typeMap: TypeMap by inject()
    private val eventRunner: EventRunner by inject()

    override val rootArchetype: Archetype = Archetype(this, typeMap, eventRunner, EntityType(), 0)
    private val archetypes = mutableListOf(rootArchetype)
    override val count: Int get() = archetypes.size

    public val archetypeCount: Int get() = archetypes.size
    private val archetypeWriteLock = SynchronizedObject()

    private fun createArchetype(prevNode: Archetype, componentEdge: ComponentId): Archetype {
        val arc = Archetype(this, typeMap, eventRunner,prevNode.type.plus(componentEdge), archetypes.size)
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
