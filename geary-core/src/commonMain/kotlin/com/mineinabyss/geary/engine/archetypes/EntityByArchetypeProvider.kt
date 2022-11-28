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
import kotlinx.atomicfu.atomic
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public class EntityByArchetypeProvider : EntityProvider, KoinComponent {
    private val removedEntities: EntityStack = EntityStack()
    private val records: TypeMap by inject()
    private val archetypeProvider: ArchetypeProvider by inject()

    private val currId = atomic(0L)
    override fun newEntity(initialComponents: Collection<Component>): GearyEntity {
        val entity = try {
            removedEntities.pop()
        } catch (e: Exception) {
            currId.getAndIncrement().toGeary()
        }
        createRecord(entity, initialComponents)
        return entity
    }

    override fun removeEntity(entity: Entity) {
        if(!entity.has<SuppressRemoveEvent>()) entity.callEvent {
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

    private fun createRecord(entity: Entity, initialComponents: Collection<Component>) {
        val ids =
            initialComponents.map { componentId(it::class) } + initialComponents.map { componentId(it::class) or HOLDS_DATA }
        val addTo = archetypeProvider.getArchetype(EntityType(ids))
        val record = Record(archetypeProvider.rootArchetype, -1)
        addTo.addEntityWithData(
            record,
            initialComponents.toTypedArray().apply { sortBy { addTo.indexOf(componentId(it::class)) } },
            entity,
        )
        records[entity] = record
    }

    override fun getType(entity: Entity): EntityType = records[entity].archetype.type
}
