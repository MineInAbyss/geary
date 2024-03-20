package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.ArrayTypeMap
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityMutateOperations
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.archetypes

class ArchetypeMutateOperations : EntityMutateOperations {
    private val records get() = archetypes.records
    private val archetypeProvider: ArchetypeProvider get() = archetypes.archetypeProvider

    override fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    ) {
        records.runOn(entity) { archetype, row ->
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val componentWithRole = componentId.withRole(HOLDS_DATA)
            archetype.setComponent(row, componentWithRole, data, !noEvent)
        }
    }

    override fun addComponentFor(
        entity: Entity,
        componentId: ComponentId,
        noEvent: Boolean
    ) {
        records.runOn(entity) { archetype, row ->
            archetype.addComponent(row, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
    }

    override fun extendFor(entity: Entity, base: Entity) {
        records.runOn(base) { archetype, row ->
            records.runOn(entity) { entityArch, entityRow ->
                archetype.instantiateTo(row, entityArch, entityRow)
            }
        }
    }

    override fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean {
        val a = records.runOn(entity) { archetype, row ->
            archetype.removeComponent(row, componentId.withRole(HOLDS_DATA))
        }
        val b = records.runOn(entity) { archetype, row ->
            archetype.removeComponent(row, componentId.withoutRole(HOLDS_DATA))
        }
        return a || b // return whether anything was changed
    }

    override fun clearEntity(entity: Entity) {
        records.runOn(entity) { archetype, row ->
            archetype.removeEntity(row)
            val newRow = archetypeProvider.rootArchetype.createWithoutData(entity)
            records.set(entity, archetypes.archetypeProvider.rootArchetype, newRow)
        }
    }
}
