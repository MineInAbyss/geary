package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityStack
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.events.types.OnEntityRemoved
import com.mineinabyss.geary.helpers.*
import com.mineinabyss.geary.modules.geary
import kotlinx.atomicfu.atomic

class EntityByArchetypeProvider(
    private val reuseIDsAfterRemoval: Boolean = true,
) : EntityProvider {
    private lateinit var records: ArrayTypeMap

    //    private val archetypeProvider: ArchetypeProvider by lazy { archetypes.archetypeProvider }
    private lateinit var root: Archetype

    private val removedEntities: EntityStack = EntityStack()
    private val currId = atomic(0L)

    override fun create(): GearyEntity {
        val entity: GearyEntity = if (reuseIDsAfterRemoval) {
            removedEntities.pop() ?: currId.getAndIncrement().toGeary()
        } else currId.getAndIncrement().toGeary()

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
