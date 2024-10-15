package com.mineinabyss.geary.engine.archetypes

import co.touchlab.stately.concurrency.AtomicLong
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeMutateOperations
import com.mineinabyss.geary.engine.archetypes.operations.ArchetypeReadOperations
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.observers.EventRunner
import com.mineinabyss.geary.observers.events.OnEntityRemoved

class EntityRemove(
    private val removedEntities: EntityStack,
    private val reader: ArchetypeReadOperations,
    private val write: ArchetypeMutateOperations,
    private val records: ArrayTypeMap,
    private val components: Components,
    private val eventRunner: EventRunner,
    private val queryManager: ArchetypeQueryManager,
){
    /** Removes an entity, freeing up its entity id for later reuse. */
    fun remove(entity: EntityId) {
        if (!reader.has(entity, components.suppressRemoveEvent))
            eventRunner.callEvent(components.onEntityRemoved, null, NO_COMPONENT, entity)

        // remove all children of this entity from the ECS as well
        if (reader.has(entity, components.couldHaveChildren)) entity.apply {
            queryManager.childrenOf(entity).forEach {
                val parents = reader.parentsOf(it)
                // Remove self from the child's parents or remove the child if it no longer has parents
                if (parents == ulongArrayOf(this)) remove(it)
                else write.removeComponentFor(it, Relation.of(components.childOf, this).id, false)
            }
        }

        // Emit remove events for each component (they get cleared all at once after this)
        records.getType(entity).forEach { compId ->
            if (reader.has(entity, compId))
                eventRunner.callEvent(components.onRemove, null, compId, entity)
        }

        records.runOn(entity) { archetype, row ->
            archetype.removeEntity(row)
            records.remove(entity)
            removedEntities.push(entity)
        }
    }
}

class EntityByArchetypeProvider(
    private val reuseIDsAfterRemoval: Boolean = true,
) : EntityProvider {
    private lateinit var records: ArrayTypeMap

    //    private val archetypeProvider: ArchetypeProvider by lazy { archetypes.archetypeProvider }
    private lateinit var root: Archetype

    internal val removedEntities: EntityStack = EntityStack()
    private val currId = AtomicLong(0L)

    override fun create(): EntityId {
        val entity: EntityId = if (reuseIDsAfterRemoval) {
            removedEntities.popOrElse { (currId.incrementAndGet() - 1).toULong() }
        } else (currId.incrementAndGet() - 1).toULong()

        createRecord(entity)
        return entity
    }

    fun init(records: ArrayTypeMap, root: Archetype) {
        this.records = records
        this.root = root
    }

    private fun createRecord(entity: EntityId) {
        val root = root
        val row = root.createWithoutData(entity)
        records[entity, root] = row
    }
}
