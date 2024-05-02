package com.mineinabyss.geary.engine.archetypes

import androidx.collection.getOrElse
import androidx.collection.set
import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.modules.archetypes

class SimpleArchetypeProvider : ArchetypeProvider {
    private val queryManager: ArchetypeQueryManager get() = archetypes.queryManager

    override val rootArchetype: Archetype by lazy {
        Archetype(EntityType(), 0).also {
            queryManager.registerArchetype(it)
        }
    }

    private val archetypeWriteLock = Synchronizable()


    private fun createArchetype(prevNode: Archetype, componentEdge: ComponentId): Archetype {
        val arc = Archetype(prevNode.type.plus(componentEdge), queryManager.archetypeCount)

        arc.componentRemoveEdges[componentEdge.toLong()] = prevNode
        prevNode.componentAddEdges[componentEdge.toLong()] = arc
        queryManager.registerArchetype(arc)
        return arc
    }

    override fun getArchetype(entityType: EntityType): Archetype = archetypeWriteLock.synchronize {
        var node = rootArchetype
        entityType.forEach { compId ->
            node = node.componentAddEdges.getOrElse(compId.toLong()) { createArchetype(node, compId) }
        }
        return@synchronize node
    }
}
