package com.mineinabyss.geary.engine.archetypes

import co.touchlab.stately.concurrency.AtomicLong
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityStack
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.events.OnEntityRemoved

class EntityByArchetypeProvider(
    private val reuseIDsAfterRemoval: Boolean = true,
) : EntityProvider {
    private lateinit var records: ArrayTypeMap

    //    private val archetypeProvider: ArchetypeProvider by lazy { archetypes.archetypeProvider }
    private lateinit var root: Archetype

    private val removedEntities: EntityStack = EntityStack()
    private val currId = AtomicLong(0L)

    override fun create(): GearyEntity {
        val entity: GearyEntity = if (reuseIDsAfterRemoval) {
            removedEntities.popOrElse { (currId.incrementAndGet() - 1).toGeary() }
        } else (currId.incrementAndGet() - 1).toGeary()

        createRecord(entity)
        return entity
    }

    override fun remove(entity: Entity) {
        if (!entity.has(geary.components.suppressRemoveEvent))
            entity.emit(geary.components.onEntityRemoved, OnEntityRemoved(), NO_COMPONENT)

        // remove all children of this entity from the ECS as well
        if (entity.has(geary.components.couldHaveChildren)) entity.apply {
            children.fastForEach {
                // Remove self from the child's parents or remove the child if it no longer has parents
                if (it.parents == setOf(this)) it.removeEntity()
                else it.removeParent(this)
            }
        }

        // Emit remove events for each component (they get cleared all at once after this)
        entity.type.forEach { compId ->
            if (entity.has(compId)) entity.emit(event = geary.components.onRemove, involving = compId)
        }

        records.runOn(entity) { archetype, row ->
            archetype.removeEntity(row)
            records.remove(entity)
            removedEntities.push(entity)
        }
    }

    fun init(records: ArrayTypeMap, root: Archetype) {
        this.records = records
        this.root = root
    }

    override fun getType(entity: Entity): EntityType = records.runOn(entity) { archetype, _ ->
        archetype.type
    }

    private fun createRecord(entity: Entity) {
        val root = root
        val row = root.createWithoutData(entity)
        records[entity, root] = row
    }
}
