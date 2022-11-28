package com.mineinabyss.geary.engine.archetypes.operations

import com.mineinabyss.geary.datatypes.*
import com.mineinabyss.geary.datatypes.maps.TypeMap
import com.mineinabyss.geary.engine.EntityMutateOperations
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

public class ArchetypeMutateOperations : EntityMutateOperations, KoinComponent {
    private val records: TypeMap by inject()

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

    override fun removeComponentFor(entity: Entity, componentId: ComponentId): Boolean =
        records[entity].run {
            val a = archetype.removeComponent(this, componentId.withRole(HOLDS_DATA))
            val b = archetype.removeComponent(this, componentId.withoutRole(HOLDS_DATA))
            a || b // return whether anything was changed
        }

    override fun clearEntity(entity: Entity) {
        val (archetype, row) = getRecord(entity)
        archetype.removeEntity(row)
        archetypeProvider.rootArchetype.addEntityWithData(getRecord(entity), arrayOf(), entity)
    }
}
