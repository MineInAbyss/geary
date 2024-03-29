package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityMutateOperations
import com.mineinabyss.geary.engine.archetypes.ArchetypeProvider
import com.mineinabyss.geary.modules.archetypes

class ArchetypeMutateOperations : EntityMutateOperations {
    private val records: TypeMap get() = archetypes.records
    private val archetypeProvider: ArchetypeProvider get() = archetypes.archetypeProvider

    override fun setComponentFor(
        entity: Entity,
        componentId: ComponentId,
        data: Component,
        noEvent: Boolean
    ) {
        records[entity].apply {
            // Only add HOLDS_DATA if this isn't a relation. All relations implicitly hold data currently and that bit
            // corresponds to the component part of the relation.
            val componentWithRole = componentId.withRole(HOLDS_DATA)
            archetype.setComponent(this, componentWithRole, data, !noEvent)
        }
    }

    override fun addComponentFor(
        entity: Entity,
        componentId: ComponentId,
        noEvent: Boolean
    ) {
        records[entity].apply {
            archetype.addComponent(this, componentId.withoutRole(HOLDS_DATA), !noEvent)
        }
    }

    override fun extendFor(entity: Entity, base: Entity) {
        val prefabRec = records[base]
        prefabRec.archetype.instantiateTo(prefabRec, records[entity])
    }

    override fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        records[entity].run {
            val a = archetype.removeComponent(this, componentId.withRole(HOLDS_DATA))
            val b = archetype.removeComponent(this, componentId.withoutRole(HOLDS_DATA))
            a || b // return whether anything was changed
        }

    override fun clearEntity(entity: Entity) {
        val record = records[entity]
        record.archetype.removeEntity(record.row)
        archetypeProvider.rootArchetype.createWithoutData(entity, record)
    }
}
