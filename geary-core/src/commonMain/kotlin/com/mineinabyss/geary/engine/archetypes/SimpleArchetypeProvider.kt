package com.mineinabyss.geary.engine.archetypes

import androidx.collection.getOrElse
import androidx.collection.set
import co.touchlab.stately.concurrency.Synchronizable
import co.touchlab.stately.concurrency.synchronize
import com.mineinabyss.geary.datatypes.ComponentId
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.observers.EventRunner

class SimpleArchetypeProvider(
    private val records: ArrayTypeMap,
    private val write: ArchetypeMutateOperations,
    private val eventRunner: EventRunner,
    private val components: Components,
    private val queryManager: ArchetypeQueryManager,
) : ArchetypeProvider {
    override val rootArchetype: Archetype by lazy {
        createArchetype(EntityType(), 0).also {
            queryManager.registerArchetype(it)
        }
    }

    private val archetypeWriteLock = Synchronizable()


    private fun createArchetype(prevNode: Archetype, componentEdge: ComponentId): Archetype {
        val arc = createArchetype(prevNode.type.plus(componentEdge), queryManager.archetypeCount)
        arc.componentRemoveEdges[componentEdge.toLong()] = prevNode
        prevNode.componentAddEdges[componentEdge.toLong()] = arc
        queryManager.registerArchetype(arc)
        return arc
    }

    private fun createArchetype(type: EntityType, id: Int): Archetype {
        return Archetype(
            type = type,
            id = id,
            write = write,
            records = records,
            archetypeProvider = this,
            eventRunner = eventRunner,
            comps = components,
            queryManager = queryManager
        )
    }

    override fun getArchetype(entityType: EntityType): Archetype = archetypeWriteLock.synchronize {
        var node = rootArchetype
        entityType.forEach { compId ->
            node = node.componentAddEdges.getOrElse(compId.toLong()) { createArchetype(node, compId) }
        }
        return@synchronize node
    }
}
