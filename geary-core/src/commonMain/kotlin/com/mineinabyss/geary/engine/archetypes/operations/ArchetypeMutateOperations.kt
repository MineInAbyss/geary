package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.engine.Components
import com.mineinabyss.geary.engine.EntityMutateOperations
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.engine.archetypes.ArchetypeQueryManager
import com.mineinabyss.geary.engine.archetypes.SimpleArchetypeProvider
import com.mineinabyss.geary.observers.EventRunner

class ArchetypeMutateOperations(
    private val records: ArrayTypeMap,
    eventRunner: EventRunner,
    components: Components,
    queryManager: ArchetypeQueryManager,
) : EntityMutateOperations {
    val archetypeProvider: ArchetypeProvider = SimpleArchetypeProvider(
        records = records,
        write = this,
        eventRunner = eventRunner,
        components = components,
        queryManager = queryManager,
    )

    override fun setComponentFor(
        entity: EntityId,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean,
    ) {
        records.runOn(entity) { archetype, row ->
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val componentWithRole = componentId.withRole(HOLDS_DATA)
            archetype.setComponent(row, componentWithRole, data, !noEvent)
        }
    }

    override fun addComponentFor(
        entity: EntityId,
        componentId: ComponentId,
        noEvent: Boolean,
    ) {
        records.runOn(entity) { archetype, row ->
            archetype.addComponent(row, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
    }

    override fun extendFor(entity: EntityId, base: EntityId) {
        records.runOn(base) { archetype, row ->
            records.runOn(entity) { entityArch, entityRow ->
                archetype.instantiateTo(row, entityArch, entityRow)
            }
        }
    }

    override fun removeComponentFor(entity: EntityId, componentId: ComponentId, noEvent: Boolean): Boolean {
        val a = records.runOn(entity) { archetype, row ->
            archetype.removeComponent(row, componentId.withRole(HOLDS_DATA), !noEvent)
        }
        val b = records.runOn(entity) { archetype, row ->
            archetype.removeComponent(row, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
        return a || b // return whether anything was changed
    }

    @Deprecated("Use removeComponentFor(entity, componentId, noEvent) instead.")
    override fun removeComponentFor(entity: EntityId, componentId: ComponentId): Boolean =
        removeComponentFor(entity, componentId, false)

    override fun clearEntity(entity: EntityId) {
        records.runOn(entity) { archetype, row ->
            archetype.removeEntity(row)
            val newRow = archetypeProvider.rootArchetype.createWithoutData(entity)
            records[entity, archetypeProvider.rootArchetype] = newRow
        }
    }
}
