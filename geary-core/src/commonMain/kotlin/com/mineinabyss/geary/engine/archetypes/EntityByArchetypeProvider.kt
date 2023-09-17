package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityStack
import com.mineinabyss.geary.datatypes.EntityType
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.helpers.parents
import com.mineinabyss.geary.helpers.removeParent
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.archetypes
import com.mineinabyss.geary.modules.geary
import kotlinx.atomicfu.atomic

class EntityByArchetypeProvider(
    private val reuseIDsAfterRemoval: Boolean = true,
) : EntityProvider {
    private val records: TypeMap by lazy { archetypes.records }
    private val archetypeProvider: ArchetypeProvider by lazy { archetypes.archetypeProvider }

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
        if (!entity.has(geary.components.suppressRemoveEvent)) entity.callEvent {
            add(geary.components.entityRemoved)
        }

        // remove all children of this entity from the ECS as well
        if (entity.has(geary.components.couldHaveChildren)) entity.apply {
            children.forEach {
                // Remove self from the child's parents or remove the child if it no longer has parents
                if (it.parents == setOf(this)) it.removeEntity()
                else it.removeParent(this)
            }
        }

        val (archetype, row) = records[entity]
        archetype.removeEntity(row)

        records.remove(entity)
        removedEntities.push(entity)
    }

    override fun getType(entity: Entity): EntityType = records[entity].archetype.type

    private fun createRecord(entity: Entity) {
        val root = archetypeProvider.rootArchetype
        val createdRecord = root.createWithoutData(entity)
        records[entity] = createdRecord
    }
}
