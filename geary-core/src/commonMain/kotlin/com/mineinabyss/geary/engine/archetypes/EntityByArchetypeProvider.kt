package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.components.CouldHaveChildren
import com.mineinabyss.geary.components.events.EntityRemoved
import com.mineinabyss.geary.components.events.SuppressRemoveEvent
import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.parents
import com.mineinabyss.geary.helpers.removeParent
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.archetypes
import kotlinx.atomicfu.atomic

class EntityByArchetypeProvider(
    private val reuseIDsAfterRemoval: Boolean = true,
) : EntityProvider {
    private val records: TypeMap get() = archetypes.records
    private val archetypeProvider: ArchetypeProvider get() = archetypes.archetypeProvider

    private val removedEntities: EntityStack = EntityStack()
    private val currId = atomic(0L)

    override fun create(initialComponents: Collection<Component>): GearyEntity {
        val entity: GearyEntity = if (reuseIDsAfterRemoval) {
            runCatching { removedEntities.pop() }
                .getOrElse { currId.getAndIncrement().toGeary() }
        } else currId.getAndIncrement().toGeary()

        createRecord(entity, initialComponents)
        return entity
    }

    override fun remove(entity: Entity) {
        if (!entity.has<SuppressRemoveEvent>()) entity.callEvent {
            add<EntityRemoved>()
        }

        // remove all children of this entity from the ECS as well
        if (entity.has<CouldHaveChildren>()) entity.apply {
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

    private fun createRecord(entity: Entity, initialComponents: Collection<Component>) {
        val ids =
            initialComponents.map { componentId(it::class) } +
                    initialComponents.map { componentId(it::class) or HOLDS_DATA }

        val addTo = archetypeProvider.getArchetype(EntityType(ids))
        val record = Record(archetypeProvider.rootArchetype, -1)
        addTo.addEntityWithData(
            record,
            initialComponents.toTypedArray().apply { sortBy { addTo.indexOf(componentId(it::class)) } },
            entity,
        )
        records[entity] = record
    }
}
