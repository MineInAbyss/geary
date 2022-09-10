package com.mineinabyss.geary.engine.archetypes

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityProvider
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.helpers.toGeary
import kotlinx.atomicfu.atomic
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public class EntityByArchetypeProvider(
    private val removedEntities: EntityStack = EntityStack()
) : EntityProvider, KoinComponent {
    private val typeMap: TypeMap by inject()
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


    private fun createRecord(entity: Entity, initialComponents: Collection<Component>) {
        val ids = initialComponents.map { componentId(it::class) } + initialComponents.map { componentId(it::class) or HOLDS_DATA }
        val addTo = archetypeProvider.getArchetype(EntityType(ids))
        val record = Record(archetypeProvider.rootArchetype, -1)
        addTo.addEntityWithData(
            record,
            initialComponents.toTypedArray().apply { sortBy { addTo.indexOf(componentId(it::class)) } },
            entity,
        )
        typeMap[entity] = record
    }

    override fun removeEntity(entity: Entity) {
        typeMap.remove(entity)
        removedEntities.push(entity)
    }
}
